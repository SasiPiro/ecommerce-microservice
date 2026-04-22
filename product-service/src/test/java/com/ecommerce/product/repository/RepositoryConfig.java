package com.ecommerce.product.repository;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.product.repository")
@EntityScan(basePackages = "com.ecommerce.product.model")
class RepositoryConfig {}
