package com.gly_gateway.service.triton.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "gly_gateway.models.gemma")
public record GemmaConfig(int maxTokens, float temperature, float topP) {}
