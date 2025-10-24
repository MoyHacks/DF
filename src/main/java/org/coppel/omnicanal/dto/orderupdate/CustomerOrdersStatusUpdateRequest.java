package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import javax.annotation.Nullable;
import java.io.Serializable;
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Getter
@Setter
@ToString
@DefaultCoder(AvroCoder.class)
public class CustomerOrdersStatusUpdateRequest implements Serializable {
    @Nullable

    @JsonProperty("CustomerOrder")
    private CustomerOrderRequest customerOrder;
    @Nullable

    @JsonProperty("EventCatalog")
    private EventCatalogRequest eventCatalog;
    @Nullable

    @JsonProperty("EventDetail")
    private EventDetailRequest eventDetail;
    @Nullable

    @JsonProperty("EventDetailLineItem")
    private EventDetailLineItem eventDetailLineItem;

}
