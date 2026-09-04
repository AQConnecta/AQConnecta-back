# syntax=docker/dockerfile:1.7

# ---- Build ----
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache de dependências
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests clean package \
    && cp target/*.jar /tmp/app.jar

# ---- Runtime ----
FROM eclipse-temurin:21.0.5_11-jre-alpine

# Atualiza pacotes do SO para receber patches de segurança e instala curl para o healthcheck
RUN apk -U upgrade --no-cache && \
    apk add --no-cache curl tini && \
    addgroup -S app && adduser -S app -G app

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

WORKDIR /app
COPY --from=build --chown=app:app /tmp/app.jar /app/app.jar

RUN mkdir -p /var/data/uploads && chown -R app:app /var/data/uploads

USER app
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS "http://localhost:${SERVER_PORT}/actuator/health" || exit 1

# Usa shell form para que ${JAVA_OPTS} e ${SERVER_PORT} sejam expandidos.
# tini garante reaping de processos e tratamento correto de sinais.
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -jar /app/app.jar"]
