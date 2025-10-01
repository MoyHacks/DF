package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@DefaultCoder(AvroCoder.class)
public class EventCatalogRequest  implements Serializable {

    @JsonProperty("EventID")
    private Integer eventID;


    @JsonProperty("EventOwner")
    private String eventOwner;

    @JsonProperty("EventName")
    private String eventName;

    @JsonProperty("EventOrigin")
    private String eventOrigin;

    @JsonProperty("Version")
    private String version;


    @JsonProperty("Timestamp")
    private String timestamp;


}
