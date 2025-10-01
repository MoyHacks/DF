package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@DefaultCoder(AvroCoder.class)
public class CustomerOrderStateRequest implements Serializable {

    @JsonProperty("CustomerOrderStateCode")
    private Long customerOrderStateCode;


    @JsonProperty("CustomerOrderStateName")
    private String customerOrderStateName;


    @JsonProperty("OrderStatusTracking")
    private String orderStatusTracking;


}
