# Simple runtime image with Java 21
FROM amazoncorretto:21-alpine

WORKDIR /app

# JAR CodeBuild se aayega (target/config-server.jar)
COPY target/*.jar app.jar


# Config server port
EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
