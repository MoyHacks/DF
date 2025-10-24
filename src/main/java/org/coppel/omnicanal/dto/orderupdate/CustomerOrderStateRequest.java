package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@DefaultCoder(AvroCoder.class)
public class CustomerOrderStateRequest implements Serializable {

    @JsonProperty("CustomerOrderStateCode")
    private Long customerOrderStateCode;


    @JsonProperty("CustomerOrderStateName")
    private String customerOrderStateName;


    @JsonProperty("OrderStatusTracking")
    private String orderStatusTracking;


}
