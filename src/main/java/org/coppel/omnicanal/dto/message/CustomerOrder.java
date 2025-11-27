package org.coppel.omnicanal.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.Data;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@DefaultCoder(AvroCoder.class)
public class CustomerOrder implements Serializable {

    @JsonProperty("customerOrderUUID")
    private String customerOrderUUID;

    @JsonProperty("customerOrderID")
    private Long customerOrderID;

    @JsonProperty("omsBY")
    private String omsBY;

    @JsonProperty("cartID")
    private int cartID;

    @JsonProperty("customerOrderStateCode")
    private CodeNameDescription customerOrderStateCode;

    @JsonProperty("customerOrderTypeCode")
    private CodeNameDescriptionType customerOrderTypeCode;

    @JsonProperty("channel")
    private CodeNameDescription channel;

    @JsonProperty("createdTimestamp")
    private String createdTimestamp;

    @JsonProperty("customerID")
    private String customerID;

    @JsonProperty("customerFirstName")
    private String customerFirstName;

    @JsonProperty("customerLastName")
    private String customerLastName;

    @JsonProperty("telephoneNumberStatic")
    private String telephoneNumberStatic;

    @JsonProperty("telephoneNumberCellPhone")
    private String telephoneNumberCellPhone;

    @JsonProperty("emailAddress")
    private String correo;

    @JsonProperty("orgId")
    private String orgId;

    @JsonProperty("orderVersion")
    private String orderVersion;

    @JsonProperty("reservationID")
    private String reservationID;

    @JsonProperty("customerOrderShipment")
    private List<CustomerOrderShipment> customerOrderShipment;

    @JsonProperty("customerOrderLineItem")
    private List<CustomerOrderLineItem> customerOrderLineItem;

    @JsonProperty("customerOrderProductLineItem")
    private List<CustomerOrderProductLineItem> customerOrderProductLineItem;

    public String toJson(){return new Gson().toJson(this);}
}
