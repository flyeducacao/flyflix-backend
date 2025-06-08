# Etapa 1: build com Maven
FROM maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /app

# Copiar o POM e baixar dependências antes do código (cache otimizado)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar o código e empacotar a aplicação
COPY src ./src
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

# Etapa 2: imagem final, apenas com o JAR
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar o JAR gerado na etapa anterior
COPY --from=builder /app/target/app.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
