package com.ecommerce.user.bin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication(scanBasePackages = {
		"com.ecommerce.user.constant",
		"com.ecommerce.user.controller",
		"com.ecommerce.user.service",
		"com.ecommerce.user.dto",
		"com.ecommerce.user.mapper",
		"com.ecommerce.user.security",
		"com.ecommerce.user.utils",
		"com.ecommerce.user.exception",
		"com.ecommerce.user.config"})
@EnableJpaRepositories(basePackages = "com.ecommerce.user.repository")
@EntityScan(basePackages = "com.ecommerce.user.model")
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
