package org.coppel.omnicanal.parser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.TupleTag;
import org.coppel.omnicanal.dto.message.CustomerOrder;
import org.coppel.omnicanal.dto.orderupdate.ActualizarStatusPedidoRefactorRequest;
import org.coppel.omnicanal.dto.statuscatalog.StatusDetail;
import org.coppel.omnicanal.utils.ActualizarStatusRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
public class ParseJsonToDtoFn extends DoFn<String, ActualizarStatusPedidoRefactorRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(ParseJsonToDtoFn.class);

    private transient ObjectMapper objectMapper;
    private final Map<String, StatusDetail> statusCatalog;

    public static final TupleTag<ActualizarStatusPedidoRefactorRequest> EXITO_TAG = new TupleTag<>() {};
    public static final TupleTag<ActualizarStatusPedidoRefactorRequest> FALLO_TAG = new TupleTag<>() {};

    public ParseJsonToDtoFn(Map<String, StatusDetail> statusCatalog) {
        this.statusCatalog = statusCatalog;
    }

    @Setup
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @ProcessElement
    public void processElement(@Element String jsonMessage, MultiOutputReceiver out) {
        ActualizarStatusPedidoRefactorRequest request = new ActualizarStatusPedidoRefactorRequest();
        try {

            CustomerOrder dto = objectMapper.readValue(jsonMessage, CustomerOrder.class);

            if (LOG.isInfoEnabled()) {
                LOG.info("Procesando mensaje de Orden: {}", dto.getCustomerOrderID());
            }

            request = new ActualizarStatusRequestBuilder()
                    .withData(dto, statusCatalog)
                    .build();


            if (request != null && request.getCustomerOrderLineItems() != null && !request.getCustomerOrderLineItems().isEmpty()) {
                out.get(EXITO_TAG).output(request);
            } else {
                LOG.warn("Orden {} no contiene campos válidos (status 0 o SKU incorrecto). Enviando a fallos.",
                        dto.getCustomerOrderID());
                out.get(FALLO_TAG).output(request);
            }

        } catch (JsonProcessingException e) {
            out.get(FALLO_TAG).output(request);
        }
    }
}