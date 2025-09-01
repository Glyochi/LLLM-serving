package com.glygateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.glygateway.service.triton.config")
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
