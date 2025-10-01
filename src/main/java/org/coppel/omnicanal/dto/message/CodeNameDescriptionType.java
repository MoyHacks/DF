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
public class CodeNameDescriptionType implements Serializable {

    @JsonProperty("code")
    private int code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("typeValue")
    private String typeValue;
}