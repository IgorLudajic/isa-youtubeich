# syntax=docker/dockerfile:1.7

ARG MAVEN_VERSION=3.9.6
ARG TEMURIN_JDK_IMAGE=eclipse-temurin:25
ARG TEMURIN_JRE_IMAGE=eclipse-temurin:25

FROM maven:${MAVEN_VERSION}-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM ${TEMURIN_JRE_IMAGE} AS runtime
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

