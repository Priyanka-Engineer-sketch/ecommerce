# Simple runtime image with Java 21
FROM amazoncorretto:21-alpine

WORKDIR /app

# JAR CodeBuild se aayega (target/config-server.jar)
COPY target/config-server.jar app.jar

EXPOSE 8888   # jo bhi tumhara config server port hai

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
