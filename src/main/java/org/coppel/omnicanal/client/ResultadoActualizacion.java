package org.coppel.omnicanal.client;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;
import org.coppel.omnicanal.dto.orderupdate.ActualizarStatusPedidoRefactorRequest;
import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoActualizacion implements Serializable {

    private boolean exito;
    private String respuesta;
    private String mensajeError;
    private String codigoError;
    private ActualizarStatusPedidoRefactorRequest requestOriginal;


    public static ResultadoActualizacion exitoso(String respuesta, ActualizarStatusPedidoRefactorRequest requestOriginal) {
        return new ResultadoActualizacion(true, respuesta, "", "0", requestOriginal);
    }

    public static ResultadoActualizacion fallido(String mensajeError, String codigoError, ActualizarStatusPedidoRefactorRequest requestOriginal) {

        return new ResultadoActualizacion(false, "Error", mensajeError, codigoError, requestOriginal);
    }

    @Override
    public String toString() {
        return "ResultadoActualizacion{" +
                "exito=" + exito +
                ", respuesta='" + respuesta + '\'' +
                ", mensajeError='" + mensajeError + '\'' +
                ", codigoError='" + codigoError + '\'' +
                '}';
    }
}