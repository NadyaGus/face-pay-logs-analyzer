# Build stage - using pre-built JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar from generator module
COPY generator/target/generator-0.0.1-SNAPSHOT.jar app.jar

# Change owner
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Dbootstrap.servers=kafka:29092", "-jar", "app.jar"]
