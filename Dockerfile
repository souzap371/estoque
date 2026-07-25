FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd --system sistemav \
    && useradd --system --gid sistemav --home-dir /app sistemav \
    && mkdir -p /app/uploads /app/logs \
    && chown -R sistemav:sistemav /app

COPY --from=build --chown=sistemav:sistemav \
    /workspace/target/estoque-0.0.1-SNAPSHOT.jar /app/estoque.jar

USER sistemav

EXPOSE 8383

ENTRYPOINT ["java", "-jar", "/app/estoque.jar"]
