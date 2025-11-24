# Simple runtime image with Java 21
FROM amazoncorretto:21-alpine

WORKDIR /app

# JAR CodeBuild se aayega (target/payment-service.jar)
COPY target/*.jar app.jar


# Config server port
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
