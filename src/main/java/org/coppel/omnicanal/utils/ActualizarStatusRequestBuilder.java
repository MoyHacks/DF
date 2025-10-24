package org.coppel.omnicanal.utils;



import org.coppel.omnicanal.dto.message.CustomerOrder;
import org.coppel.omnicanal.dto.message.CustomerOrderLineItem;
import org.coppel.omnicanal.dto.orderupdate.*;
import org.coppel.omnicanal.dto.statuscatalog.StatusDetail;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ActualizarStatusRequestBuilder {

    CustomerOrder order;
    Map<String, StatusDetail>  statusCatalog;

    public ActualizarStatusRequestBuilder withData(CustomerOrder order,Map<String, StatusDetail>  statusCatalog) {
        this.order = order;
        this.statusCatalog = statusCatalog;
        return this;
    }

    public ActualizarStatusPedidoRefactorRequest build() {
        ActualizarStatusPedidoRefactorRequest request = new ActualizarStatusPedidoRefactorRequest();
        List<CustomerOrderLineItems> ropaItems = getCustomerOrderLineItems();
        if (ropaItems.isEmpty()) {
            setNotValid(request);
            return request;
        }
        StatusDetail statusDetail = statusCatalog.get(String.valueOf(this.order.getCustomerOrderStateCode().getCode()));
        request.setCustomerOrderID(this.order.getCustomerOrderID());
        request.setCustomerID(Long.valueOf(this.order.getCustomerID()));
        request.setStatusCode(statusDetail.getStatus());
        request.setTypeUpdate(2);
        request.setCustomerOrderLineItems(ropaItems);
        CustomerOrdersStatusUpdateRequest orderstatus = new CustomerOrdersStatusUpdateRequest();
            GeneralRequest generalRequest = new GeneralRequest((long) this.order.getCustomerOrderStateCode().getCode(),(long)0);
            orderstatus.setCustomerOrder(new CustomerOrderRequest(this.order.getCustomerOrderID(),generalRequest));
            orderstatus.setEventDetail( getEventDetail(statusDetail));
            orderstatus.setEventCatalog( new EventCatalogRequest(1,"di-com-statusUpdate","Update Status","OMS","1.0",fechaActual()));
            orderstatus.setEventDetailLineItem(getEventDetailLineItems());
        request.setCustomerOrdersStatusUpdate(orderstatus);
        return request;
    }

    private EventDetailRequest getEventDetail(StatusDetail statusDetail){
        CustomerOrderStateRequest state = new CustomerOrderStateRequest((long)statusDetail.getStatus(),statusDetail.getStatusName(),statusDetail.getStatusTracking());
       return new EventDetailRequest(state);
    }

    private EventDetailLineItem getEventDetailLineItems(){
        List<CustomerOrderLineItemDTO>  data = this.order.getCustomerOrderLineItem()
                .stream()
                .map(lineItem -> {
                    CustomerOrderLineItemDTO item = new CustomerOrderLineItemDTO();
                    item.setItemID(getPartNumber(lineItem.getSku()));
                    item.setCustomerOrderItemID(Long.parseLong(lineItem.getSku()));
                    item.setItemStatus(getItemStates(lineItem));
                   // item.setItemQuantity(lineItem.getOrderedItemQuantity());
                    return item;
                })
                .collect(Collectors.toList());

        return new EventDetailLineItem(data);
    }

    private List<ItemStatus> getItemStates (CustomerOrderLineItem lineItem){
        List<ItemStatus> itemsStatus = new ArrayList<>();
        StatusDetail statusDetailCatalog = statusCatalog.get(String.valueOf(lineItem.getCustomerOrderLineItemStateCode().getCode()));
        CustomerOrderItemState itemState = new CustomerOrderItemState(statusDetailCatalog.getStatusProduct(),statusDetailCatalog.getStatusNameProduct(),statusDetailCatalog.getStatusTracking());
        ItemStatus itemstatus = new ItemStatus(itemState,lineItem.getOrderedItemQuantity());
        itemsStatus.add(itemstatus);
        return itemsStatus;
    }


    @NotNull
    private List<CustomerOrderLineItems> getCustomerOrderLineItems() {
        return this.order.getCustomerOrderLineItem()
                .stream()
                .filter(lineItem -> lineItem.getSku().length() == 9)
                .map(lineItem -> {
                    CustomerOrderLineItems item = new CustomerOrderLineItems();
                    item.setAreaItem(getArea(lineItem.getSku()));
                    item.setCodeRegisterID(lineItem.getCustomerOrderLineItemSequenceNumber());
                    item.setSizeItem(getSize(lineItem.getSku()));
                    item.setCustomerOrderItemID(getSku(lineItem.getSku()));
                    item.setQuantityItem(lineItem.getOrderedItemQuantity());
                    item.setStatusCodeItem(statusCatalog.get(String.valueOf(lineItem.getCustomerOrderLineItemStateCode().getCode())).getStatusProduct());
                    return item;
                })
                .collect(Collectors.toList());
    }

    public String fechaActual() {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    public int getArea(String sku){
        return switch (sku.length()) {
            case 6 -> 3;
            case 9 -> 2;
            default -> 99;
        };
    }

    public int getSize(String sku){
        if(sku.length() > 6){
            return Integer.parseInt(sku.substring(sku.length() - 3));
        }
        return 0;
    }

    public Long getSku(String sku){
        if (sku.length() > 6){
            return Long.parseLong(sku.substring(0, 6));
        }
        return Long.parseLong(sku);
    }

    public String getPartNumber(String sku){
        if(sku.length() > 6){
            return "IR-"+sku.substring(0,6)+"2-"+sku.substring(sku.length()-3);
        }
        return "IM-"+sku+"3-0";
    }

    public void setNotValid(ActualizarStatusPedidoRefactorRequest request){
        request.setStatusCode(-1);
        request.setCustomerOrderID(this.order.getCustomerOrderID());
        request.setTypeUpdate(0);
        request.setCustomerID(0L);
        request.setCustomerOrderLineItems(Collections.emptyList());
        CustomerOrdersStatusUpdateRequest orderStatus = new CustomerOrdersStatusUpdateRequest();
        GeneralRequest generalRequest = new GeneralRequest(0L, 0L);
        orderStatus.setCustomerOrder(new CustomerOrderRequest(0L, generalRequest));
        orderStatus.setEventDetail(new EventDetailRequest(new CustomerOrderStateRequest(0L,"","")));
        orderStatus.setEventCatalog(new EventCatalogRequest(0, "none", "none", "none", "0.0", "N/A"));
        orderStatus.setEventDetailLineItem(new EventDetailLineItem(Collections.emptyList()));
        request.setCustomerOrdersStatusUpdate(orderStatus);
    }

}