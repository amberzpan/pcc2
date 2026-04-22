package com.pcc2.social.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserSchemaCompatibilityService {

    private final JdbcTemplate jdbcTemplate;

    public UserSchemaCompatibilityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized boolean apply() {
        if (jdbcTemplate == null) {
            return false;
        }

        boolean changed = false;
        changed = ensureColumn("user", "cover_url", "ALTER TABLE user ADD COLUMN cover_url VARCHAR(255) DEFAULT NULL") || changed;
        return changed;
    }

    private boolean ensureColumn(String tableName, String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute(alterSql);
            return true;
        }
        return false;
    }
}
