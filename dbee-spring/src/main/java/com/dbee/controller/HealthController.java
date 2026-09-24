package com.dbee.controller;

import com.dbee.config.AppProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbcTemplate;
    private final AppProperties properties;

    public HealthController(JdbcTemplate jdbcTemplate, AppProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "ok");

        // Database test
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            status.put("database", "connected");
            try {
                Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
                status.put("usersTable", "exists (registered users: " + count + ")");
            } catch (Exception e) {
                status.put("usersTable", "MISSING or error: " + e.getMessage());
            }
        } catch (Exception e) {
            status.put("database", "FAILED: " + e.getMessage());
        }

        // JWT Secret check
        if (properties.jwtSecret() == null || properties.jwtSecret().trim().isEmpty()) {
            status.put("jwtSecret", "MISSING: JWT_SECRET environment variable is empty");
        } else if (properties.jwtSecret().length() < 32) {
            status.put("jwtSecret", "TOO SHORT: JWT_SECRET must be at least 32 characters (current: " + properties.jwtSecret().length() + ")");
        } else {
            status.put("jwtSecret", "configured");
        }

        // Dialogflow Credentials file check
        String credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credsPath == null || credsPath.isBlank()) {
            status.put("dialogflowCredentials", "MISSING: GOOGLE_APPLICATION_CREDENTIALS env var not set");
        } else {
            File f = new File(credsPath);
            status.put("dialogflowCredentials", f.exists() ? "file found at " + credsPath : "FILE NOT FOUND at " + credsPath);
        }

        return status;
    }
}
