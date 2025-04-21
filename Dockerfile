# Use a slim OpenJDK base image
FROM openjdk:17-jdk-slim as build

# Set working directory
WORKDIR /app

# Copy Maven build files
COPY pom.xml .
COPY src ./src

# Build the JAR (you can cache layers by copying only pom first)
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests

# Runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the fat‑jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose HTTP port
EXPOSE 8080

# JVM options (e.g. to pick up env vars for JWT secret, Kafka)
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","/app/app.jar"]

