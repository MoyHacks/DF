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
public class ItemStatus implements Serializable {


    @JsonProperty("CustomerOrderItemState")
    private CustomerOrderItemState customerOrderItemState;
    @JsonProperty("ItemQuantity")
    private int itemQuantity;


}