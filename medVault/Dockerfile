#FROM eclipse-temurin:17-jdk
#
#COPY target/medvault.jar medvault.jar
#
#ENTRYPOINT ["java","-jar", "/medvault.jar"]

# ---- BUILD STAGE ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# copy only the medVault module
COPY medVault/pom.xml .
COPY medVault/src ./src

# build the jar
RUN mvn -DskipTests package

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# IMPORTANT: check the real jar name inside target/ after mvn package
# Example name; change if needed
COPY --from=build /app/target/medVault-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

