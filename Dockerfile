# Etapa 1: Build con Maven + JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Empaquetar jar (fat jar)
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final con launcher oficial de Apache Beam Flex
FROM gcr.io/dataflow-templates-base/java21-template-launcher-base:latest

WORKDIR /dataflow/template

# Copiar el jar generado
COPY --from=builder /app/target/*.jar app.jar

# Variable que indica la MainClass de tu pipeline
ENV FLEX_TEMPLATE_JAVA_MAIN_CLASS="org.coppel.omnicanal.PubSubToApiPipeline"
ENV FLEX_TEMPLATE_JAVA_CLASSPATH="/dataflow/template/app.jar"

# El entrypoint ya está definido en la imagen base de Dataflow



