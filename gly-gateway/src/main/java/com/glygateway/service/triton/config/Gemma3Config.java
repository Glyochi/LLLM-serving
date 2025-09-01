package com.glygateway.service.triton.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "glygateway.models.gemma3")
public record Gemma3Config(
    int maxTokens,
    float temperature,
    float topP) {
}
