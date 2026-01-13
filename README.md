
# Dataflow: di-com-statusupdate 
**Pipeline: Actualización de estado de órdenes 

## Descripción general
El proyecto di-com-statusupdate implementa un pipeline de Apache Beam (Dataflow) que consume mensajes desde Google Pub/Sub, los transforma en estructuras de dominio, y realiza llamadas seguras a un servicio REST a través de Apigee para actualizar el estado de pedidos.

## Ejecución local (DirectRunner)
Puedes probar el pipeline localmente con el siguiente comando Maven o desde  IDE.  
Esto usa tu suscripción real de Pub/Sub y las URLs del entorno **DEV**.

| Argumento             | Descripción                                                                |
|-----------------------|----------------------------------------------------------------------------|
| `--runner`            | Define el tipo de ejecución (local o Dataflow).                            |
| `--inputSubscription` | Suscripción de Pub/Sub desde la cual se leen los mensajes.                 |
| `--apiUrl`            | Endpoint REST para actualizar el estado de las órdenes.                    |
| `--apiTimeout`        | Tiempo máximo de espera (ms) para cada request HTTP.                       |
| `--apiRetries`        | Número de reintentos en caso de errores temporales.                        |
| `--apiSecretName`     | Nombre del secreto en Secret Manager que contiene las credenciales OAuth2. |
| `--project`           | ID del proyecto GCP en el cual se ejecuta el pipeline.                     |
| `--tokenApiUrl`       | Endpoint del servicio de Party Identity para obtener tokens OAuth2.        |
| `--statusCatalog`     | Ubicación del catálogo de equivalencias de estados.                        |

> Nota: Para ejecución local, puede utilizarse un catálogo de prueba dentro de `src/main/resources/`.




## Args IDE 
--runner=DirectRunner
--inputSubscription="suscripcion de pubsub"
--apiUrl=https://{env}-apigee.coppel.io/bs/com/order-update/api/v1-02/order/status
--apiTimeout=5000
--apiRetries=3
--apiSecretName="secret con las credenciales para consumir party identity"
--project="projecto gcp"
--tokenApiUrl="URL del endpoint party identity"
--statusCatalog="ubicacion del catalogo de status"

```bash
mvn compile exec:java \
  -Dexec.mainClass=org.coppel.omnicanal.PubSubToApiPipeline \
  -Dexec.args="\
    --runner=DirectRunner \
    --project="projecto gcp" \
    --inputSubscription="suscripcion de pubsub" \
    --apiUrl=https://{env}-apigee.coppel.io/bs/com/order-update/api/v1-02/order/status \
    --apiTimeout=5000 \
    --apiRetries=3 \
    --apiSecretName= "secret con las credenciales para consumir party identity" \
    --tokenApiUrl="URL del endpoint party identity" \
    --statusCatalog="ubicacion del catalogo de status o equivalencia de conversion"



