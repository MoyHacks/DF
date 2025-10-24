package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties
@DefaultCoder(AvroCoder.class)
public class CustomerOrderLineItems implements Serializable {


        @JsonProperty("StatusCodeItem")
        private int statusCodeItem;

        @JsonProperty("QuantityItem")
        private int   quantityItem;

        @JsonProperty("CodeRegisterID")
        private long   codeRegisterID;

         @JsonProperty("AreaItem")
         private int areaItem;

        @JsonProperty("SizeItem")
        private int  sizeItem;

         @JsonProperty("CustomerOrderItemID")
         private long customerOrderItemID;
}
