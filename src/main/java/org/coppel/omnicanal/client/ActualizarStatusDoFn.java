package org.coppel.omnicanal.client;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.beam.sdk.transforms.DoFn;
import org.coppel.omnicanal.dto.orderupdate.ActualizarStatusPedidoRefactorRequest;
import org.coppel.omnicanal.dto.partyidentity.TokenResponse;
import org.coppel.omnicanal.exceptions.TransientHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
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
    private transient ObjectMapper mapper;

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
        LOG.info("Inicializando instancia DoFn id={}", this.hashCode());
        this.mapper = new ObjectMapper();
        this.webClient = WebClient.builder().build();

        this.retrySpec = Retry.backoff(maxRetries, Duration.ofSeconds(1))
                .filter(throwable ->
                        throwable instanceof TransientHttpException ||
                                throwable instanceof TimeoutException)
                .onRetryExhaustedThrow((spec, signal) ->
                        new Exception("El servicio no respondió tras varios reintentos."));


        this.partyIdentityClient = new PartyIdentityClient(webClient,tokenApiUrl,clientId,clientSecret,grantType,scope);
        getValidToken();
    }

    private String getValidToken() {
        Instant now = Instant.now();
        if (authToken == null || tokenExpiryTime == null || now.isAfter(tokenExpiryTime)) {
            synchronized (this) {
                if (authToken == null || tokenExpiryTime == null || Instant.now().isAfter(tokenExpiryTime)) {
                    LOG.info(" Instancia {} | Token expirado o inexistente. Solicitando uno nuevo...",this.hashCode());
                    TokenResponse tokenResponse = partyIdentityClient.getNewToken();
                    this.authToken = tokenResponse.getAccess_token();
                    long ttl = Math.max(0, tokenResponse.getExpires_in() - 120);
                    this.tokenExpiryTime = Instant.now().plusSeconds(ttl);

                    LOG.info("Instancia {} | Nuevo token obtenido. Expira en {} segundos.",this.hashCode(), ttl);
                } else {
                    LOG.debug("Instancia {} | Token sigue siendo válido hasta {}",this.hashCode(), tokenExpiryTime);
                }
            }
        }
        return this.authToken;
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
        ActualizarStatusPedidoRefactorRequest requestBody = c.element();
        LOG.info(" Instancia {} | Procesando request: {} " , this.hashCode(), requestBody.toString());

        try {
            authToken = getValidToken();

            ResultadoActualizacion resultado = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchangeToMono(response -> {
                        HttpStatusCode status = response.statusCode();

                        return response.bodyToMono(String.class)
                                .doOnNext(body -> LOG.info("Body recibido del servicio: {}", body))
                                .map(body -> {
                                    try {
                                        JsonNode root = mapper.readTree(body);
                                        JsonNode metaNode = root.path("Response").path("meta");
                                        if (metaNode.isMissingNode() || metaNode.isNull()) {
                                            metaNode = root.path("meta");
                                        }

                                        if (metaNode.isMissingNode() || metaNode.isNull()) {
                                            return ResultadoActualizacion.fallido(
                                                    "Respuesta sin campo meta",
                                                    "999",
                                                    requestBody);
                                        }

                                        int codeHttp = metaNode.path("codeHttp").asInt();
                                        int codeService = metaNode.path("codeService").asInt();
                                        String msg = metaNode.path("messageService").asText();


                                        if (codeHttp == 200 && codeService == 0) {
                                            LOG.info("Éxito: {}", msg);
                                            return ResultadoActualizacion.exitoso(msg, requestBody);
                                        }

                                        if (codeHttp == 404 || codeService == 104) {
                                            LOG.info("️Recurso no encontrado 404: {}", msg);
                                            return ResultadoActualizacion.fallido(msg, String.valueOf(codeService), requestBody);
                                        }


                                        if (codeHttp >= 400 && codeHttp < 500) {
                                            LOG.warn("Error controlado ({}): {}", codeService, msg);
                                            return ResultadoActualizacion.fallido(msg, String.valueOf(codeService), requestBody);
                                        }


                                        if (codeHttp >= 500 || codeHttp == 429) {
                                            LOG.error(" Error recuperable ({}): {}", codeHttp, msg);
                                            throw new TransientHttpException(
                                                    "Error recuperable: " + codeHttp + " - " + msg);
                                        }

                                        LOG.warn(" Respuesta inesperada: {}", body);
                                        return ResultadoActualizacion.fallido(
                                                "Respuesta inesperada: " + codeHttp + " - " + msg,
                                                String.valueOf(codeHttp),
                                                requestBody);

                                    } catch (TransientHttpException e) {
                                        throw e;
                                    } catch (Exception e) {
                                        LOG.error("Error parseando respuesta JSON: {}", e.getMessage());
                                        return ResultadoActualizacion.fallido(
                                                "Error al parsear la respuesta del servicio",
                                                "998",
                                                requestBody);
                                    }
                                });
                    })

                    .timeout(Duration.ofMillis(timeoutMs))
                    .retryWhen(retrySpec)
                    .block();

            c.output(resultado);

        } catch (WebClientRequestException ex) {

            LOG.error("Error de conexión  para el pedido: {}.", requestBody.getCustomerOrderID(), ex);
            throw new RuntimeException("Error de infraestructura irrecuperable al contactar el endpoint. Causa: " + ex.getMessage(), ex);

        } catch (Exception e) {
            LOG.error("Error inesperado  procesando el pedido: {}", requestBody.getCustomerOrderID(), e);
            throw new RuntimeException("Error inesperado en el DoFn. Causa: " + e.getMessage(), e);
        }
    }
}