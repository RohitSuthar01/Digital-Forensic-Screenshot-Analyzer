package com.dfsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DigitalForensicScreenshotAnalyzerApplication {

    public static void main(String[] args) {
        sanitizeEnvironment();
        SpringApplication.run(DigitalForensicScreenshotAnalyzerApplication.class, args);
    }

    private static void sanitizeEnvironment() {
        cleanProp("DB_URL", "spring.datasource.url");
        cleanProp("DB_USERNAME", "spring.datasource.username");
        cleanProp("DB_PASSWORD", "spring.datasource.password");
        cleanProp("PORT", "server.port");
        cleanProp("MYSQLUSER", "spring.datasource.username");
        cleanProp("MYSQLPASSWORD", "spring.datasource.password");
    }

    private static void cleanProp(String envKey, String propKey) {
        String val = System.getenv(envKey);
        if (val == null || val.isBlank()) {
            val = System.getenv(propKey.toUpperCase().replace('.', '_'));
        }
        if (val == null || val.isBlank()) {
            val = System.getProperty(propKey);
        }
        if (val != null && !val.isBlank()) {
            val = val.trim().replaceAll("[\\r\\n]+", "");
            System.setProperty(propKey, val);
        }
    }

}