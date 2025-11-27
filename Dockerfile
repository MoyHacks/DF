# Etapa 1: Build con Maven + JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .

# Construir fat jar
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final basada en el launcher oficial de Dataflow
FROM gcr.io/dataflow-templates-base/java21-template-launcher-base:latest

WORKDIR /dataflow/template

# Copiar el jar generado
COPY --from=builder /app/target/*-bundled-*.jar app.jar

# Variables requeridas por Dataflow Flex
ENV FLEX_TEMPLATE_JAVA_MAIN_CLASS="org.coppel.omnicanal.PubSubToApiPipeline"
ENV FLEX_TEMPLATE_JAVA_CLASSPATH="/dataflow/template/app.jar"


