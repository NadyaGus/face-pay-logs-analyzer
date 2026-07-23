# Build stage - using pre-built JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar from spark-consumer module
COPY spark-consumer/target/spark-consumer-0.0.1-SNAPSHOT.jar app.jar

# Change owner
RUN chown -R appuser:appgroup /app

USER appuser

ENTRYPOINT ["java", "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED", "-Dbootstrap.servers=kafka:29092", "-Dpostgres.url=jdbc:postgresql://postgres:5432/facepay_stream", "-Dpostgres.user=admin", "-Dpostgres.password=admin", "-jar", "app.jar"]
