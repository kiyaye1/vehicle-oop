# ----------------------------
# Stage 1: Build the application
# ----------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy and resolve dependencies
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# Copy the source and build
COPY src ./src
RUN mvn -q -B -DskipTests clean package

# ----------------------------
# Stage 2: Create the runtime image
# ----------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*SNAPSHOT*.jar app.jar

# Expose application port
EXPOSE 8282

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
