package org.coppel.omnicanal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.coppel.omnicanal.client.ActualizarStatusDoFn;
import org.coppel.omnicanal.client.ResultadoActualizacion;
import org.coppel.omnicanal.dto.orderupdate.ActualizarStatusPedidoRefactorRequest;
import org.coppel.omnicanal.dto.statuscatalog.StatusDetail;
import org.coppel.omnicanal.exceptions.OrderUpdateException;
import org.coppel.omnicanal.parser.ParseJsonToDtoFn;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class PubSubToApiPipeline {

    private static final Logger LOG = LoggerFactory.getLogger(PubSubToApiPipeline.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public interface PubSubToApiOptions extends PipelineOptions, GcpOptions {

        @Description("El suscriptor de Pub/Sub del cual leer los mensajes (ej: projects/mi-proyecto/topics/entrada)")
        @Validation.Required
        String getInputSubscription();
        void setInputSubscription(String value);

        @Description("La URL del API a la que se llamará")
        @Validation.Required
        String getApiUrl();
        void setApiUrl(String value);

        @Description("Timeout en milisegundos para la llamada al API")
        @Validation.Required
        Long getApiTimeout();
        void setApiTimeout(Long value);

        @Description("Número de reintentos para la llamada al API")
        @Validation.Required
        Integer getApiRetries();
        void setApiRetries(Integer value);

        @Description("Nombre del Secret Manager que contiene el service account en formato JSON")
        @Validation.Required
        String getApiSecretName();
        void setApiSecretName(String value);

        @Description("URL PartyIdentity")
        @Validation.Required
        String getTokenApiUrl();
        void setTokenApiUrl(String value);

        @Description("Catálogo de estatus en formato String o ruta GCS")
        @Validation.Required
        String getStatusCatalog();
        void setStatusCatalog(String value);
    }
    private static String accessSecret(String projectId, String secretId) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, "latest");
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            LOG.info("Credenciales cargadas desde Secret Manager");
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception e) {
            throw new OrderUpdateException(
                    "Error al acceder al secreto: " + secretId,
                    null,
                    OrderUpdateException.ErrorType.SECRET_MANAGER,
                    "E001",
                    e
            );
        }
    }
    private static String loadCatalogFromFile(String filePath) {
        try {
            if (filePath.startsWith("gs://")) {
                Blob blob = getBlob(filePath);
                return new String(blob.getContent(), StandardCharsets.UTF_8);
            } else {
                return Files.readString(Paths.get(filePath));
            }
        } catch (IOException e) {
            throw new OrderUpdateException(
                    "No se pudo leer el catálogo desde: " + filePath,
                    null,
                    OrderUpdateException.ErrorType.CATALOG_LOAD,
                    "E002",
                    e
            );
        }
    }

    @NotNull
    private static Blob getBlob(String filePath) {
        String withoutPrefix = filePath.substring(5);
        int slashIndex = withoutPrefix.indexOf('/');
        String bucket = withoutPrefix.substring(0, slashIndex);
        String object = withoutPrefix.substring(slashIndex + 1);

        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(bucket, object);

        if (blob == null) {
            throw new OrderUpdateException(
                    "El archivo no existe en GCS: " + filePath,
                    null,
                    OrderUpdateException.ErrorType.CATALOG_LOAD,
                    "E003",
                    null
            );
        }
        return blob;
    }

    public static Map<String, StatusDetail> parsearCatalog(String jsonCatalog) {
        try {
            return objectMapper.readValue(jsonCatalog, new TypeReference<Map<String, StatusDetail>>() {});
        } catch (JsonProcessingException e) {
            throw new OrderUpdateException(
                    "Error al parsear el catálogo de estatus.",
                    null,
                    OrderUpdateException.ErrorType.CATALOG_LOAD,
                    "E004",
                    e
            );
        }
    }

    public static Map<String, String> loadToken(String creds) {
        try {
            return new ObjectMapper().readValue(creds, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new OrderUpdateException(
                    "Error al parsear el JSON del secreto (token).",
                    null,
                    OrderUpdateException.ErrorType.TOKEN_PARSE,
                    "E005",
                    e
            );
        }
    }

    public static void main(String[] args) {
        try {
            PubSubToApiOptions options = PipelineOptionsFactory.fromArgs(args)
                    .withValidation()
                    .as(PubSubToApiOptions.class);

            String stringCatalog = loadCatalogFromFile(options.getStatusCatalog());
            Map<String, StatusDetail> catalog = parsearCatalog(stringCatalog);

            String creds = accessSecret(options.getProject(), options.getApiSecretName());
            Map<String, String> token = loadToken(creds);

            Pipeline p = Pipeline.create(options);


            PCollection<ActualizarStatusPedidoRefactorRequest> dtos =
                    p.apply("1. Leer Mensajes de Pub/Sub",
                                    PubsubIO.readStrings().fromSubscription(options.getInputSubscription()))
                            .apply("2. Convertir JSON a DTO",
                                    ParDo.of(new ParseJsonToDtoFn(catalog)));

            PCollection<ActualizarStatusPedidoRefactorRequest> validos =
                    dtos.apply("Filtrar requests válidos",
                            Filter.by(req -> req != null &&
                                    req.getCustomerOrderLineItems() != null &&
                                    !req.getCustomerOrderLineItems().isEmpty()));

            PCollection<ResultadoActualizacion> sinRopa =
                    dtos.apply("Filtrar requests sin ropa",
                                    Filter.by(req -> req == null ||
                                            req.getCustomerOrderLineItems() == null ||
                                            req.getCustomerOrderLineItems().isEmpty()))
                            .apply("Mapear a ResultadoActualizacion",
                                    MapElements.into(TypeDescriptor.of(ResultadoActualizacion.class))
                                            .via(req -> ResultadoActualizacion.fallido(
                                                    "Orden sin artículos de ropa",
                                                    "NO_ROPA",
                                                    req)));

            PCollection<ResultadoActualizacion> resultados =
                    validos.apply("3. Llamar API de Actualización",
                            ParDo.of(new ActualizarStatusDoFn(
                                    options.getApiUrl(),
                                    options.getApiTimeout(),
                                    options.getApiRetries(),
                                    options.getTokenApiUrl(),
                                    token.get("tokenClientId"),
                                    token.get("tokenClientSecret"),
                                    token.get("tokenGrantType"),
                                    token.get("tokenScope")
                            )));

            PCollection<ResultadoActualizacion> exitosos = resultados.apply("4a. Filtrar Éxitos",
                    Filter.by(r -> r != null && r.isExito()));

            PCollection<ResultadoActualizacion> fallidos = PCollectionList
                    .of(sinRopa)
                    .and(resultados.apply("4b. Filtrar Fallos", Filter.by(r -> r != null && !r.isExito())))
                    .apply("Unir fallidos", Flatten.pCollections());

            exitosos.apply("Log Éxitos", ParDo.of(new DoFn<ResultadoActualizacion, Void>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                    LOG.info("Pedido {} procesado correctamente.",
                            Objects.requireNonNull(c.element())
                                    .getRequestOriginal()
                                    .getCustomerOrderID());
                }
            }));

            fallidos.apply("Log Fallos", ParDo.of(new DoFn<ResultadoActualizacion, Void>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                    ResultadoActualizacion r = c.element();
                    LOG.warn("FALLO: {} - Pedido {}",
                            r != null ? r.getMensajeError() : "Error desconocido",
                            r != null && r.getRequestOriginal() != null
                                    ? r.getRequestOriginal().getCustomerOrderID()
                                    : "desconocido");
                }
            }));


            PipelineResult result = p.run();
            if ("DirectRunner".equalsIgnoreCase(options.getRunner().getSimpleName())) {
                result.waitUntilFinish();
            }

        } catch (OrderUpdateException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderUpdateException(
                    "Error inesperado al ejecutar el pipeline",
                    null,
                    OrderUpdateException.ErrorType.UNEXPECTED,
                    "E999",
                    e
            );
        }
    }
}