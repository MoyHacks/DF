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
public class CustomerOrderShipment implements Serializable {

    @JsonProperty("transactionID")
    private int transactionID;

    @JsonProperty("bookingID")
    private String bookingID;

    @JsonProperty("customerOrderLineItemSequenceNumber")
    private int customerOrderLineItemSequenceNumber;

    @JsonProperty("lineItemDetail")
    private int lineItemDetail;

    @JsonProperty("firstNamePersonReceive")
    private String firstNamePersonReceive;

    @JsonProperty("lastNamePersonReceive")
    private String lastNamePersonReceive;

    @JsonProperty("telephoneNumberPersonReceive")
    private String telephoneNumberPersonReceive;

    @JsonProperty("street")
    private String street;

    @JsonProperty("externalNumber")
    private String externalNumber;

    @JsonProperty("internalNumber")
    private String internalNumber;

    @JsonProperty("streetReferences")
    private String streetReferences;

    @JsonProperty("aditionalReferences")
    private String aditionalReferences;

    @JsonProperty("observations")
    private String observations;

    @JsonProperty("postalCode")
    private int postalCode;

    @JsonProperty("settlementsID")
    private int settlementsID;

    @JsonProperty("settlementsName")
    private String settlementsName;

    @JsonProperty("localityID")
    private int localityID;

    @JsonProperty("localityName")
    private String localityName;

    @JsonProperty("municipalityID")
    private int municipalityID;

    @JsonProperty("municipalityName")
    private String municipalityName;



    @JsonProperty("stateID")
    private int stateID;

    @JsonProperty("stateName")
    private String stateName;

    @JsonProperty("countryID")
    private int countryID;

    @JsonProperty("countryName")
    private String countryName;

    @JsonProperty("geophysicallocationCoordinateID")
    private String geophysicallocationCoordinateID;
}