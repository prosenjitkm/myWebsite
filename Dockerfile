# Backend Dockerfile: multi-stage build using Maven to produce a runnable jar
FROM maven:3.10.1-eclipse-temurin-21 AS build
WORKDIR /workspace
# copy pom and mvn wrapper and .mvn for faster dependency resolution
COPY pom.xml mvnw ./
COPY .mvn .mvn
# copy source
COPY src ./src
# package the application (skip tests for speed)
RUN mvn -B -DskipTests package

# runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
EXPOSE 8080
ARG JAR_FILE=target/backend-0.0.1-SNAPSHOT.jar
COPY --from=build /workspace/${JAR_FILE} ./app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
