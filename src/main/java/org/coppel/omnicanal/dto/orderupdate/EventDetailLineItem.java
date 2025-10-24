package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@DefaultCoder(AvroCoder.class)
public class EventDetailLineItem  implements Serializable {

    @JsonProperty("CustomerOrderLineItems")
    @Nullable
    private List<CustomerOrderLineItemDTO> customerOrderLineItems;


}