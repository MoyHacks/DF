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
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString
@NoArgsConstructor
@Getter
@Setter
@DefaultCoder(AvroCoder.class)
public class ActualizarStatusPedidoRefactorRequest implements Serializable {

  @JsonProperty("StatusCode")
  private int statusCode;

  @JsonProperty("CustomerOrderID")
  private Long customerOrderID;


  @JsonProperty("TypeUpdate")
  private int typeUpdate;


  @JsonProperty("CustomerID")
  private Long customerID;


  @JsonProperty("CustomerOrderLineItems")
  private List<CustomerOrderLineItems> customerOrderLineItems;

  @Nullable
  @JsonProperty("CustomerOrdersStatusUpdate")
  private CustomerOrdersStatusUpdateRequest customerOrdersStatusUpdate;


}
