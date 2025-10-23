FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=build /app/target/*SNAPSHOT*.jar app.jar

ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=oracle.jdbc.OracleDriver

EXPOSE 8282
ENTRYPOINT ["java","-jar","/app/app.jar"]
