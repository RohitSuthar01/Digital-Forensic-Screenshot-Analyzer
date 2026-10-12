package com.dfsa;

import java.net.URI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DigitalForensicScreenshotAnalyzerApplication {

    public static void main(String[] args) {
        configureDatabase();
        SpringApplication.run(DigitalForensicScreenshotAnalyzerApplication.class, args);
    }

    /** Resolve Railway MySQL URIs without masking connection failures with ephemeral H2. */
    private static void configureDatabase() {
        String databaseUrl = firstNonBlank(
                System.getenv("DB_URL"),
                System.getenv("MYSQL_JDBC_URL"),
                System.getenv("DATABASE_URL"),
                System.getenv("MYSQL_URL"),
                System.getenv("SERVICE_URI"),
                System.getenv("AIVEN_URL"));

        boolean railway = firstNonBlank(System.getenv("RAILWAY_ENVIRONMENT"),
                System.getenv("RAILWAY_PROJECT_ID")) != null;
        if (databaseUrl == null) {
            if (railway) {
                throw new IllegalStateException("No database URL is configured on Railway. Set DB_URL to the MySQL service URL reference.");
            }
            return; // Local development can use the H2 default in application.properties.
        }

        databaseUrl = databaseUrl.trim().replaceAll("[\r\n]+", "");
        if (databaseUrl.startsWith("mysql://")) {
            URI uri = URI.create(databaseUrl);
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                int separator = userInfo.indexOf(':');
                if (separator >= 0) {
                    setIfMissing("spring.datasource.username", decode(userInfo.substring(0, separator)));
                    setIfMissing("spring.datasource.password", decode(userInfo.substring(separator + 1)));
                }
            }

            String path = uri.getRawPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                path = "/railway";
            }
            databaseUrl = "jdbc:mysql://" + uri.getHost() + ":" +
                    (uri.getPort() > 0 ? uri.getPort() : 3306) + path +
                    "?useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
                databaseUrl += "&" + uri.getRawQuery();
            }
        } else if (!databaseUrl.startsWith("jdbc:mysql://")) {
            throw new IllegalStateException("Configured database URL must use mysql:// or jdbc:mysql://.");
        }

        System.setProperty("spring.datasource.url", databaseUrl);
        copyEnvIfPresent("DB_USERNAME", "spring.datasource.username");
        copyEnvIfPresent("MYSQLUSER", "spring.datasource.username");
        copyEnvIfPresent("DB_PASSWORD", "spring.datasource.password");
        copyEnvIfPresent("MYSQLPASSWORD", "spring.datasource.password");
        System.out.println("[DFSA] Using configured MySQL datasource (credentials omitted).");
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private static void setIfMissing(String key, String value) {
        if (System.getProperty(key) == null || System.getProperty(key).isBlank()) {
            System.setProperty(key, value);
        }
    }

    private static void copyEnvIfPresent(String env, String property) {
        String value = System.getenv(env);
        if (value != null && !value.isBlank() &&
                (System.getProperty(property) == null || System.getProperty(property).isBlank())) {
            System.setProperty(property, value.trim().replaceAll("[\r\n]+", ""));
        }
    }

    private static String decode(String value) {
        return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
