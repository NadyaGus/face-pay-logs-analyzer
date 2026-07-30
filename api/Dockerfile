FROM maven:3.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn install -DskipTests
RUN mvn -pl api clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /api/target/api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
