package com.ecommerce.apigateway.filter;

import com.ecommerce.apigateway.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Lista endpoint pubblici
    private static final List<String>
            PUBLIC_PATHS = List.of(
            "/api/v1/auth/loginApi",
            "/api/v1/products/all",
            "/api/v1/products/price-range",
            "/api/v1/products/search",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/webjars",
            "/swagger-resources");

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Skip endpoint pubblici
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 2. Leggi header Authorization
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // 3. Niente token -> 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        // 4. Valida il token
        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return unauthorized(exchange);
        }
        // Estrai claims
        String username = jwtUtil.extractUsername(token);
        Long userId = jwtUtil.extractUserId(token);
        List<String> roles = jwtUtil.extractRoles(token);
        List<String> permissions = jwtUtil.extractPermissions(token);

        //Add user info to headers for downstream services
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId.toString())
                .header("X-Username", username)
                .header("X-User-Roles", String.join(",",roles))
                .header("X-User-Permissions", String.join(",",permissions))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

        // Helper: risposta 401
        private Mono<Void> unauthorized(ServerWebExchange exchange) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        // Helper: check path pubblico
        private boolean isPublicPath(String p) {
            return PUBLIC_PATHS.stream().anyMatch(p::startsWith);
        }

        @Override
        public int getOrder() {
            return 1;
        }
}



