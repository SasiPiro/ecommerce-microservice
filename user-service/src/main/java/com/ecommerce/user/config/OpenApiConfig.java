package com.ecommerce.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Overrides springdoc's auto-generated server URL.
 * Forces the Swagger UI to use "/" (relative to the gateway host) so that all
 * requests pass through the API Gateway instead of hitting :8081 directly.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/").description("API Gateway")
                ));
    }
}
