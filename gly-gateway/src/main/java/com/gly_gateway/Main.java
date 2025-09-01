package com.gly_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.gly_gateway.service.triton.config")
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
