# Build stage
FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# Run stage
FROM amazoncorretto:21-alpine
WORKDIR /app

COPY --from=build /app/target/config-server.jar app.jar

EXPOSE 0
ENTRYPOINT ["java","-jar","/app/app.jar"]

