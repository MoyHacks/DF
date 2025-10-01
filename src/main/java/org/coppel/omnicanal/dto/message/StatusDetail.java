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
public class StatusDetail implements Serializable {

    @JsonProperty("statusMacroid")
    private int statusMacroid;

    @JsonProperty("statusMacroDescription")
    private String statusMacroDescription;

    @JsonProperty("statusCarrierDescription")
    private String statusCarrierDescription;

    @JsonProperty("statusParcelDescription")
    private String statusParcelDescription;

    @JsonProperty("updatedAt")
    private String updatedAt;
}