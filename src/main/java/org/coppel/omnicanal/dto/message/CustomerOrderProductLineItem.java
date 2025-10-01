package org.coppel.omnicanal.dto.message;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@DefaultCoder(AvroCoder.class)
public class CustomerOrderProductLineItem implements Serializable {

    @JsonProperty("customerOrderLineItemSequenceNumber")
    private int customerOrderLineItemSequenceNumber;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("shortDescription")
    private String shortDescription;

    @JsonProperty("orderedItemQuantity")
    private int orderedItemQuantity;

    @JsonProperty("fulFilledItemQuantity")
    private int fulFilledItemQuantity;

    @JsonProperty("dateAssortment")
    private String dateAssortment;

    @JsonProperty("estimatedAvailabilityDate")
    private String estimatedAvailabilityDate;

    @JsonProperty("actualAvailabilityDate")
    private String actualAvailabilityDate;

    @JsonProperty("quotationID")
    private String quotationID;

    @JsonProperty("saleUnitRetailPriceAmount")
    private double saleUnitRetailPriceAmount;
}