# Simple runtime image with Java 21
FROM amazoncorretto:21-alpine

WORKDIR /app

# JAR CodeBuild se aayega (target/cart-service.jar)
COPY target/*.jar app.jar


# Config server port
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
