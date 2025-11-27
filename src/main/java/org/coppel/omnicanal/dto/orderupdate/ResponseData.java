package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseData<T extends Serializable> implements Serializable {
  private Meta meta;
  private T data;
}
