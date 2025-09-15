package com.glygateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import reactor.core.publisher.Hooks;

@SpringBootApplication
// Scan TritonConfig to be autowired to TritonGrpcConfig
@ConfigurationPropertiesScan(basePackages = "com.glygateway.service.triton.config")
public class Main {

  public static void main(String[] args) {
    // Needed for context for logger (trace/span)
    Hooks.enableAutomaticContextPropagation();
    SpringApplication.run(Main.class, args);
  }

}
