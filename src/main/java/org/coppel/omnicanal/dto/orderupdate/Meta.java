package org.coppel.omnicanal.dto.orderupdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta implements Serializable {
    private int codeService;// CODIGO DE RESPUESTA DE LA APLICACION
    private String messageService;// MENSAJE QUE MOSTRAMOS AL CLIENTE
    private String transactionID;// ID DE LA TRANSACCION
    private int codeHttp;// CODIGO HTTP
    private String messageHttp;// MENSAJE DEL CODIGO HTTP
    private String timestamp;// FECHA Y HORA DEL MOMENTO EN EL QUE SE GENERA EL RESPONSE
    private String timeDuration;// TIEMPO QUE DURO EJECUTANDODE EL ENDPOINT
}
