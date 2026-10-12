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

# Install Tesseract OCR and English language data
RUN apt-get update && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-eng && rm -rf /var/lib/apt/lists/*

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata

# Copy built JAR from builder stage
COPY --from=builder /app/target/digital-forensic-screenshot-analyzer-1.0.0.jar app.jar

EXPOSE 8091
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8091} -jar app.jar"]
