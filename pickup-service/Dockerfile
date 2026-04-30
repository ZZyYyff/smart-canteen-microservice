FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/pickup-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9004
ENTRYPOINT ["java", "-jar", "app.jar"]
