# Etapa 1: Build con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar pom.xml y settings.xml antes del código para cachear dependencias
COPY pom.xml ./  

# Descargar dependencias offline
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar y generar jar
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final para Dataflow Flex Template
FROM gcr.io/dataflow-templates-base/java21-template-launcher-base:latest

WORKDIR /dataflow/template

# Copiar jar generado
COPY --from=builder /app/target/*-bundled-*.jar app.jar

# Variables obligatorias para Dataflow Flex Template
ENV FLEX_TEMPLATE_JAVA_MAIN_CLASS="org.coppel.PubSubToApiPipeline"
ENV FLEX_TEMPLATE_JAVA_CLASSPATH="/dataflow/template/app.jar"


