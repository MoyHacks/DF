package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import javax.annotation.Nullable;
import java.io.Serializable;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@DefaultCoder(AvroCoder.class)
public class EventDetailRequest implements Serializable {
    @Nullable

    @JsonProperty("CustomerOrderState")
    private CustomerOrderStateRequest customerOrderStateRequest;

}
