#FROM eclipse-temurin:17-jdk
#
#COPY target/medvault.jar medvault.jar
#
#ENTRYPOINT ["java","-jar", "/medvault.jar"]

# ---- BUILD STAGE ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY medVault/pom.xml .
COPY medVault/src ./src

RUN mvn -DskipTests package

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/medvault.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]


