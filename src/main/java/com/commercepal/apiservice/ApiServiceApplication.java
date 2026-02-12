package com.commercepal.apiservice;

import com.commercepal.apiservice.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiServiceApplication {

	public static void main(String[] args) {
		EnvFileLoader.loadIfPresent();
		SpringApplication.run(ApiServiceApplication.class, args);
	}

}
