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

@DefaultCoder(AvroCoder.class)
public class CustomerOrderRequest implements Serializable {

    @JsonProperty("CustomerOrderID")
    private Long customerOrderID;

    @Nullable
    @JsonProperty("General")
    private GeneralRequest general;

}
