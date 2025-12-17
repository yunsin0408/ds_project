package com.example.isoogle;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.isoogle", "com.example.stage3", "com.example.stage4", "com.example.stage5"})
public class IsoogleApplication {

	public static void main(String[] args) {
		SpringApplication.run(IsoogleApplication.class, args);
	}

	@Bean
	public CommandLineRunner startupInfo() {
		return args -> {
			boolean envExists = new java.io.File(".env").exists();
			System.out.println("IsoogleApplication started. .env file present: " + envExists);
			System.out.println("Available endpoints: GET /api/cse?query=...&mode=semantic|iterative|cse");
		};
	}

}
