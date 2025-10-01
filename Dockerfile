# Etapa 1: Build con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar pom.xml primero (para cachear dependencias)
COPY pom.xml ./
COPY settings.xml /root/.m2/settings.xml

# Copiar todo el código fuente
COPY src ./src

# Compilar y generar jar
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final para Dataflow
FROM gcr.io/dataflow-templates-base/java21-template-launcher-base:latest

WORKDIR /dataflow/template

COPY --from=builder /app/target/*-bundled-*.jar app.jar

ENV FLEX_TEMPLATE_JAVA_MAIN_CLASS="org.coppel.omnicanal.PubSubToApiPipeline"
ENV FLEX_TEMPLATE_JAVA_CLASSPATH="/dataflow/template/app.jar"

