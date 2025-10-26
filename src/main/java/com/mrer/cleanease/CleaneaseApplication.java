package com.mrer.cleanease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.mrer.cleanease.repository")
public class CleaneaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(CleaneaseApplication.class, args);
	}

}
