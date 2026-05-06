package com.ecommerce.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Overrides springdoc's auto-generated server URL.
 *
 * Without this bean, springdoc infers the server URL from the incoming request
 * (e.g. http://localhost:8082). When the API Gateway Swagger UI fetches the
 * spec
 * via /v3/api-docs/product-service and gets that URL, it makes requests
 * directly
 * to :8082 instead of routing through the gateway — causing cross-origin
 * "Failed
 * to fetch" errors on secured endpoints.
 *
 * By setting url = "/" the Swagger UI resolves the base URL relative to
 * wherever
 * it is hosted (the gateway at :8989), keeping all traffic inside the gateway.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/").description("API Gateway")));
    }
}
