package com.glygateway.service.triton.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "glygateway.models.gemma")
public record GemmaConfig(
    @NotNull @Min(0) int maxTokens,
    @NotNull @Min(0) float temperature,
    @NotNull @Min(0) float topP) {
}
