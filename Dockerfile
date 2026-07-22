# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source code
COPY src/ ./src/

# Build the application
RUN mvn -B clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar from build stage
COPY --from=build /build/target/facepay-logs-analyzer-0.0.1-SNAPSHOT.jar app.jar

# Change owner
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED", "-Dbootstrap.servers=kafka:29092", "-jar", "app.jar"]
