# Multi-stage build for Digital Forensic Screenshot Analyzer
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-eng && rm -rf /var/lib/apt/lists/*
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata
RUN mkdir -p /app/tessdata && cp /usr/share/tesseract-ocr/4.00/tessdata/eng.traineddata /app/tessdata/eng.traineddata
COPY --from=builder /app/target/digital-forensic-screenshot-analyzer-1.0.0.jar app.jar
EXPOSE 8091
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8091} -jar app.jar"]
