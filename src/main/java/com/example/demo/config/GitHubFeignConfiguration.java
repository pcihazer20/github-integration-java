package com.example.demo.config;

import feign.Retryer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GitHubFeignConfiguration {

    private final GitHubRetryProperties retryProperties;

    @Bean
    public Retryer retryer() {
        log.info("Configuring GitHub Feign Retryer with maxAttempts={}, initialInterval={}ms, maxInterval={}ms, multiplier={}",
                retryProperties.getMaxAttempts(),
                retryProperties.getInitialIntervalMs(),
                retryProperties.getMaxIntervalMs(),
                retryProperties.getMultiplier());

        return new Retryer.Default(
                retryProperties.getInitialIntervalMs(),
                retryProperties.getMaxIntervalMs(),
                retryProperties.getMaxAttempts()
        );
    }
}