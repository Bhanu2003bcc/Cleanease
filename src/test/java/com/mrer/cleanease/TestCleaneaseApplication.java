package com.mrer.cleanease;

import org.springframework.boot.SpringApplication;

public class TestCleaneaseApplication {

	public static void main(String[] args) {
		SpringApplication.from(CleaneaseApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
