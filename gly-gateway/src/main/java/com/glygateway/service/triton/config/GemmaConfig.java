package com.glygateway.service.triton.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "glygateway.models.gemma")
public record GemmaConfig(int maxTokens, float temperature, float topP) {}
