package com.ecommerce.apigateway.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Global OpenAPI Configuration for the API Gateway.
 * Provides central Swagger documentation and a unified JWT SecurityScheme.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "E-Commerce Microservices Platform API", version = "v1", description = "Centralized API documentation. Requests made through this Swagger UI are routed directly via the API Gateway."))
public class OpenApiConfig {
}
