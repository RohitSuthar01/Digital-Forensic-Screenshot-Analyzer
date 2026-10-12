package com.dfsa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class DatabaseSchemaMigration implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String databaseProduct = jdbcTemplate.execute(
                (ConnectionCallback<String>) connection -> connection.getMetaData().getDatabaseProductName());
        if (databaseProduct == null || !databaseProduct.equalsIgnoreCase("MySQL")) {
            return;
        }

        Integer alreadyText = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = 'analysis_results' " +
                        "AND column_name = 'verdict_explanation' AND data_type = 'text'",
                Integer.class);
        if (alreadyText == null || alreadyText == 0) {
            jdbcTemplate.execute("ALTER TABLE analysis_results MODIFY COLUMN verdict_explanation TEXT NULL");
            log.info("Expanded analysis_results.verdict_explanation to TEXT for long analysis summaries.");
        }
    }
}
