
package com.glygateway.service.triton.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "triton")
public record TritonConfig(
    @NotNull String host,
    @NotNull int httpPort,
    @NotNull int grpcPort,
    @NotNull int timeoutMs) {
}
