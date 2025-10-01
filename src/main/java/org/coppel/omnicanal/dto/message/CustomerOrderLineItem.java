package org.coppel.omnicanal.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@DefaultCoder(AvroCoder.class)
public class CustomerOrderLineItem implements Serializable {

    @JsonProperty("bookingID")
    private String bookingID;

    @JsonProperty("transactionID")
    private int transactionID;

    @JsonProperty("customerOrderLineItemSequenceNumber")
    private int customerOrderLineItemSequenceNumber;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("orderedItemQuantity")
    private int orderedItemQuantity;

    @JsonProperty("retailTransactionLineItemSequenceNumber")
    private int retailTransactionLineItemSequenceNumber;

    @JsonProperty("customerOrderLineItemStateCode")
    private CodeNameDescription customerOrderLineItemStateCode;

    @JsonProperty("typeCodeDeliveryCustomer")
    private int typeCodeDeliveryCustomer;

    @JsonProperty("typeCodeSale")
    private int typeCodeSale;

    @JsonProperty("typeCodeAssortment")
    private int typeCodeAssortment;

    @JsonProperty("typeCodeReceive")
    private int typeCodeReceive;

    @JsonProperty("businessUnitIDSale")
    private int businessUnitIDSale;

    @JsonProperty("businessUnitIDDeliveryCustomer")
    private int businessUnitIDDeliveryCustomer;

    @JsonProperty("businessUnitIDAssortment")
    private int businessUnitIDAssortment;

    @JsonProperty("businessUnitIDReceive")
    private int businessUnitIDReceive;

    @JsonProperty("createdTimestamp")
    private String createdTimestamp;

    @JsonProperty("estimatedDeliveryDateMin")
    private String estimatedDeliveryDateMin;

    @JsonProperty("estimatedDeliveryDateMax")
    private String estimatedDeliveryDateMax;

    @JsonProperty("fullfilmentType")
    private String fullfilmentType;

    @JsonProperty("fullfilmentService")
    private String fullfilmentService;

    @JsonProperty("customerOrderLineItemTypeCode")
    private CodeNameDescriptionType customerOrderLineItemTypeCode;

    @JsonProperty("customerOrderLineItemStatusDetail")
    private List<StatusDetail> customerOrderLineItemStatusDetail;
}