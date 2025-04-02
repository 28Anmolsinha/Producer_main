
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY Backend_producer-main/target/demo-0.0.1-SNAPSHOT.jar app/demo-0.0.1-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "app/demo-0.0.1-SNAPSHOT.jar"]
