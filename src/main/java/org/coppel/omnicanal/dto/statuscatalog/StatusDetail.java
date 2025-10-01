package org.coppel.omnicanal.dto.statuscatalog;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@DefaultCoder(AvroCoder.class)
public class StatusDetail  implements Serializable {
    @JsonProperty("status")
    private int status;

    @JsonProperty("statusProduct")
    private int statusProduct;

    @JsonProperty("statusNameProduct")
    private String statusNameProduct;

    @JsonProperty("statusName")
    private String statusName;

    @JsonProperty("statusTracking")
    private String statusTracking;
}
