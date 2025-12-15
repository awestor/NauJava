FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

COPY src/main/resources/logback-spring.xml src/main/resources/

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app
RUN chown -R spring:spring /app

USER spring:spring

COPY --from=build --chown=spring:spring /app/target/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]