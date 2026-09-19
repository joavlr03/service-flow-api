# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Cloud Run injects PORT (default 8080); our app.yml already reads ${PORT:9000}.
# Flags below trade peak throughput for faster cold start, which matters more
# on a scale-to-zero service than steady-state performance.
ENV JAVA_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
