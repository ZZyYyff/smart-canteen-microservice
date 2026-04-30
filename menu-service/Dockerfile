FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/menu-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9002
ENTRYPOINT ["java", "-jar", "app.jar"]
