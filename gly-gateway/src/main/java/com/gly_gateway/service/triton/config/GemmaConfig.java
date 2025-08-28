package com.gly_gateway.service.triton.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class GemmaConfig {

  private int maxTokens = 8192;
  private float temperature = 1.0f;
  private float topP = 0.95f;

  public int getMaxTokens() {
    return maxTokens;
  }

  public float getTemperature() {
    return temperature;
  }

  public float getTopP() {
    return topP;
  }

}
