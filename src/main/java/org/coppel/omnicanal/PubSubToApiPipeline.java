package org.coppel.omnicanal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.coppel.omnicanal.client.ActualizarStatusDoFn;
import org.coppel.omnicanal.client.ResultadoActualizacion;
import org.coppel.omnicanal.dto.orderupdate.ActualizarStatusPedidoRefactorRequest;
import org.coppel.omnicanal.dto.statuscatalog.StatusDetail;
import org.coppel.omnicanal.parser.ParseJsonToDtoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;


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

        @Description("Url PartyIdentity")
        @Validation.Required
        String getTokenApiUrl();
        void setTokenApiUrl(String value);

        @Description("El Client ID para la autenticación del token")
        @Validation.Required
        String getTokenClientId();
        void setTokenClientId(String value);

        @Description("Secret party Identity")
        @Validation.Required
        String getTokenClientSecret();
        void setTokenClientSecret(String value);

        @Description("Grant type party Identity")
        @Validation.Required
        String getTokenGrantType();
        void setTokenGrantType(String value);

        @Description("Scope party Identity")
        @Validation.Required
        String getTokenScope();
        void setTokenScope(String value);

        @Description("Catálogo de estatus en formato String")
        @Validation.Required
        String getStatusCatalog();
        void setStatusCatalog(String value);

    }

    private static String accessSecret(String projectId, String secretId, String version) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, version);
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception e) {
            throw new RuntimeException("Error al acceder al secreto " + secretId, e);
        }
    }

    private static GoogleCredentials loadCredentialsFromSecret(String projectId, String secretId, String version) {
        try {
            String jsonKey = accessSecret(projectId, secretId, version);


            new ObjectMapper().readValue(jsonKey, Map.class);

            return GoogleCredentials.fromStream(
                    new ByteArrayInputStream(jsonKey.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron cargar credenciales desde el secreto " + secretId, e);
        }
    }

    public static void main(String[] args) {
        PubSubToApiOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(PubSubToApiOptions.class);
        
        String stringCatalog = loadCatalogFromFile(options.getStatusCatalog());
        Map<String, StatusDetail> catalog = parsearCatalog(stringCatalog);
        // 1. Cargar credenciales desde Secret Manager
        GoogleCredentials creds = loadCredentialsFromSecret(
                options.getProject(),
                options.getApiSecretName(),
                "latest"
        );

        options.as(GcpOptions.class).setGcpCredential(creds);
        LOG.info("Credenciales personalizadas cargadas desde Secret Manager.");

        Pipeline p = Pipeline.create(options);


        PCollection<ActualizarStatusPedidoRefactorRequest> dtos =
                p.apply("1. Leer Mensajes de Pub/Sub",
                                PubsubIO.readStrings().fromSubscription(options.getInputSubscription()))
                        .apply("2. Convertir JSON a DTO",
                                ParDo.of(new ParseJsonToDtoFn(catalog)));

        PCollection<ResultadoActualizacion> resultados =
                dtos.apply("3. Llamar API de Actualización",
                        ParDo.of(new ActualizarStatusDoFn(
                                options.getApiUrl(),
                                options.getApiTimeout(),
                                options.getApiRetries(),
                                options.getTokenApiUrl(),
                                options.getTokenClientId(),
                                options.getTokenClientSecret(),
                                options.getTokenGrantType(),
                                options.getTokenScope()
                        )));

        PCollection<ResultadoActualizacion> exitosos = resultados.apply("4a. Filtrar Éxitos",
                Filter.by(ResultadoActualizacion::isExito));

        PCollection<ResultadoActualizacion> fallidos = resultados.apply("4b. Filtrar Fallos",
                Filter.by(r -> !r.isExito()));

        exitosos.apply("Log Éxitos", ParDo.of(new DoFn<ResultadoActualizacion, Void>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                LOG.info("ÉXITO: " + c.element().getRequestOriginal().getCustomerOrderID());
            }
        }));

        fallidos.apply("Log Fallos", ParDo.of(new DoFn<ResultadoActualizacion, Void>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                LOG.warn("FALLO: " + c.element().getMensajeError()
                        + " para el pedido: " + c.element().getRequestOriginal().getCustomerOrderID());
            }
        }));

        PipelineResult result = p.run();
        result.waitUntilFinish();
    }

    public static Map<String, StatusDetail> parsearCatalog(String jsonCatalog) {
        try {
            return objectMapper.readValue(jsonCatalog, new TypeReference<Map<String, StatusDetail>>() {});
        } catch (JsonProcessingException e) {
            LOG.error("Error al parsear el catálogo de estatus. El formato JSON podría ser incorrecto.", e);
            return Collections.emptyMap();
        }
    }
   private static String loadCatalogFromFile(String filePath) {
        try {
            if (filePath.startsWith("gs://")) {
                String withoutPrefix = filePath.substring(5); // quitar "gs://"
                int slashIndex = withoutPrefix.indexOf('/');
                String bucket = withoutPrefix.substring(0, slashIndex);
                String object = withoutPrefix.substring(slashIndex + 1);

                Storage storage = StorageOptions.getDefaultInstance().getService();
                Blob blob = storage.get(bucket, object);
                if (blob == null) {
                    throw new RuntimeException("El archivo no existe en GCS: " + filePath);
                }
                return new String(blob.getContent(), StandardCharsets.UTF_8);
            } else {
                return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el catálogo desde: " + filePath, e);
        }
    }

}



