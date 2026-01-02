package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "github.retry")
public class GitHubRetryProperties {
    private int maxAttempts = 5;
    private long initialIntervalMs = 1000;
    private long maxIntervalMs = 10000;
    private double multiplier = 2.0;
}

