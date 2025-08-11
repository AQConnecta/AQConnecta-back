# ---- Build ----
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests clean package

# ---- Runtime ----
FROM eclipse-temurin:21-jre-alpine
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="" \
    JAVA_HOME=/opt/java/openjdk
# usuário não-root
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
USER app
# use o binário absoluto — evita depender do PATH
ENTRYPOINT ["/opt/java/openjdk/bin/java","-Dserver.port=${SERVER_PORT}","-jar","/app/app.jar"]
