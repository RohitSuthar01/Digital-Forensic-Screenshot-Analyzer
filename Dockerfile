# Multi-stage build for Digital Forensic Screenshot Analyzer
# Stage 1: Build the Java Spring Boot Application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy backend source
COPY backend/pom.xml .
COPY backend/src ./src

# Package JAR without running tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime image with OpenJDK 17 and Tesseract OCR
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install Tesseract OCR and native libraries
RUN apt-get update && \
    apt-get install -y --no-install-recommends tesseract-ocr && \
    rm -rf /var/lib/apt/lists/*

# Copy built JAR from builder stage
COPY --from=builder /app/target/digital-forensic-screenshot-analyzer-1.0.0.jar app.jar

# Expose port (Render sets $PORT dynamically, Spring Boot defaults to 8091 or respects PORT)
EXPOSE 8091

# Start the application
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8091} -jar app.jar"]
