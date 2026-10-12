package com.dfsa;

import java.net.URI;
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
        // 1. Check for standard database URI (Aiven Service URI, Railway MYSQL_URL, or DATABASE_URL)
        String rawUri = getFirstEnv("DATABASE_URL", "AIVEN_URL", "MYSQL_URL", "SERVICE_URI");
        if (rawUri == null || rawUri.isBlank()) {
            String dbUrlEnv = System.getenv("DB_URL");
            if (dbUrlEnv != null && dbUrlEnv.trim().startsWith("mysql://")) {
                rawUri = dbUrlEnv.trim();
            }
        }

        if (rawUri != null && !rawUri.isBlank()) {
            try {
                String cleanUri = rawUri.trim().replaceAll("[\\r\\n]+", "");
                if (cleanUri.startsWith("mysql://")) {
                    URI uri = URI.create(cleanUri.replace("mysql://", "http://"));
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        System.setProperty("spring.datasource.username", parts[0].trim());
                        System.setProperty("spring.datasource.password", parts[1].trim());
                    }
                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 3306;
                    String path = uri.getPath() != null && !uri.getPath().isEmpty() ? uri.getPath() : "/defaultdb";
                    String jdbcUrl = "jdbc:mysql://" + host + ":" + port + path + "?useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                    System.setProperty("spring.datasource.url", jdbcUrl);
                }
            } catch (Exception ignored) {
                // Fall through to manual property cleanup
            }
        }

        // 2. Fall back to individual properties if not already set by URI parser
        cleanProp("DB_URL", "spring.datasource.url");
        cleanProp("DB_USERNAME", "spring.datasource.username");
        cleanProp("DB_PASSWORD", "spring.datasource.password");
        cleanProp("PORT", "server.port");
        cleanProp("MYSQLUSER", "spring.datasource.username");
        cleanProp("MYSQLPASSWORD", "spring.datasource.password");

        // 3. Fix common typos in spring.datasource.url if present
        String currentUrl = System.getProperty("spring.datasource.url");
        if (currentUrl != null) {
            String fixedUrl = currentUrl.trim()
                    .replaceAll("[\\r\\n]+", "")
                    .replaceFirst("^jdbc\\.mysql[\\./]+", "jdbc:mysql://");
            System.setProperty("spring.datasource.url", fixedUrl);
        }
    }

    private static String getFirstEnv(String... keys) {
        for (String k : keys) {
            String v = System.getenv(k);
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static void cleanProp(String envKey, String propKey) {
        if (System.getProperty(propKey) != null) {
            return;
        }
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