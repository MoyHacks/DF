package org.coppel.omnicanal.parser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.beam.sdk.transforms.DoFn;
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

    public ParseJsonToDtoFn(Map<String, StatusDetail> statusCatalog) {
        this.statusCatalog = statusCatalog;
    }

    @Setup
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
        String jsonMessage = c.element();
        try {
            CustomerOrder dto = objectMapper.readValue(
                    jsonMessage,
                    CustomerOrder.class
            );
            ActualizarStatusPedidoRefactorRequest request = new ActualizarStatusRequestBuilder().withData(dto, statusCatalog).build();
            c.output(request);

        } catch (JsonProcessingException e) {
            LOG.error("No se pudo parsear el JSON: " + jsonMessage, e);
        }
    }
}