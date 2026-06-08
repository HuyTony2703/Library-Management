package com.library.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public String health() {
        return "Backend is running";
    }

    @GetMapping("/api/warmup")
    public String warmup() {
        jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return "Backend is warm";
    }
}
