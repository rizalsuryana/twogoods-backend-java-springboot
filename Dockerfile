
# =====================================
# BUILD STAGE
# =====================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests

# =====================================
# RUNTIME STAGE
# =====================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user (security)
RUN useradd -ms /bin/bash spring
USER spring

# Copy JAR
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# JVM flags optimized for container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75", \
  "-jar", "app.jar"]
