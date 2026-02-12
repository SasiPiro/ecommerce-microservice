package com.ecommerce.user.repository;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.user.repository")
@EntityScan(basePackages = "com.ecommerce.user.model")
class RepositoryConfig {}