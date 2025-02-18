# Use OpenJDK 23 as base image
FROM openjdk:23-jdk-slim

# Set working directory in container
WORKDIR /app

# Copy Maven build files
COPY target/cloudtalk-0.0.2-SNAPSHOT.jar app-java.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app-java.jar"]