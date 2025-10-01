package org.coppel.omnicanal.client;


import org.apache.beam.sdk.transforms.DoFn;
import org.coppel.omnicanal.dto.orderupdate.ActualizarStatusPedidoRefactorRequest;
import org.coppel.omnicanal.dto.partyidentity.TokenResponse;
import org.coppel.omnicanal.utils.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

public class ActualizarStatusDoFn extends DoFn<ActualizarStatusPedidoRefactorRequest, ResultadoActualizacion> {

    private static final Logger LOG = LoggerFactory.getLogger(ActualizarStatusDoFn.class);


    private final String url;
    private final long timeoutMs;
    private final int maxRetries;

    private String tokenApiUrl;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String scope;


    private transient WebClient webClient;
    private transient Retry retrySpec;
    private transient PartyIdentityClient partyIdentityClient;
    private transient String authToken;
    private transient Instant tokenExpiryTime;

    public ActualizarStatusDoFn(String url, long timeoutMs, int maxRetries, String tokenApiUrl,String clientId,String clientSecret,String grantType,String scope) {
        this.url = url;
        this.timeoutMs = timeoutMs;
        this.maxRetries = maxRetries;
        this.tokenApiUrl = tokenApiUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.scope = scope;
    }

    @Setup
    public void setup() {

        webClient = WebClient.builder().build();

        retrySpec = Retry.backoff(maxRetries, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof WebClientRequestException || throwable instanceof TimeoutException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        new Exception("El servicio de actualización no respondió después de varios reintentos. "+ "503 "+ Constantes.BS_COM_ORDER_UPDATE)
                );

        this.partyIdentityClient = new PartyIdentityClient(this.webClient,this.tokenApiUrl,this.clientId,this.clientSecret,this.grantType,this.scope);
        getValidToken();
    }

    private String getValidToken() {
        synchronized (this) {
            if (authToken == null || Instant.now().isAfter(tokenExpiryTime)) {
                LOG.info("Token expirado. Solicitando uno nuevo...");
                TokenResponse tokenResponse = partyIdentityClient.getNewToken();
                this.authToken = tokenResponse.getAccess_token();
                this.tokenExpiryTime = Instant.now().plusSeconds(tokenResponse.getExpires_in() - 60);
                LOG.info("Nuevo token obtenido.");
            }
        }
        return this.authToken;
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
        ActualizarStatusPedidoRefactorRequest requestBody = c.element();
        LOG.info("Procesando request: " + requestBody.toString());

        try {
            authToken = getValidToken();

            ResultadoActualizacion resultado = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchangeToMono(response -> {
                        if (response.statusCode().isError()) {
                            return response.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.just(ResultadoActualizacion.fallido(
                                            "El servicio respondió con error: " + errorBody,
                                            String.valueOf(response.statusCode().value()),
                                            requestBody
                                    )));
                        } else {
                            return response.bodyToMono(String.class)
                                    .map(successBody -> ResultadoActualizacion.exitoso(successBody, requestBody));
                        }
                    })
                    .timeout(Duration.ofMillis(timeoutMs))
                    .retryWhen(retrySpec)
                    .block();

            c.output(resultado);

        } catch (WebClientRequestException ex) {

            LOG.error("Error de conexión FATAL para el pedido: {}.", requestBody.getCustomerOrderID(), ex);
            throw new RuntimeException("Error de infraestructura irrecuperable al contactar el endpoint. Causa: " + ex.getMessage(), ex);

        } catch (Exception e) {

            LOG.error("Error inesperado FATAL procesando el pedido: {}", requestBody.getCustomerOrderID(), e);
            throw new RuntimeException("Error inesperado en el DoFn. Causa: " + e.getMessage(), e);
        }
    }
}