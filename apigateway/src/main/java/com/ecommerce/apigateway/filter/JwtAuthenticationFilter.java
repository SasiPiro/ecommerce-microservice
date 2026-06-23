package com.ecommerce.apigateway.filter;

import static com.ecommerce.apigateway.security.Permission.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final Set<String> DEFAULT_KEYCLOAK_ROLES = Set.of(
            "offline_access", "default-roles-microservices-realm", "uma_authorization"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    Jwt jwt = auth.getToken();
                    String path = exchange.getRequest().getURI().getPath();
                    String method = exchange.getRequest().getMethod().name();

                    // Estrai claim dal JWT Keycloak
                    String userId = jwt.getSubject();
                    String username = jwt.getClaimAsString("preferred_username");
                    String email = jwt.getClaimAsString("email");

                    // Estrai realm_access.roles
                    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                    List<String> allRoles = List.of();
                    if (realmAccess != null && realmAccess.get("roles") != null) {
                        allRoles = ((List<?>) realmAccess.get("roles")).stream()
                                .map(Object::toString)
                                .toList();
                    }

                    // Separa ruoli da permissions (convenzione: permissions contengono ".")
                    List<String> roles = allRoles.stream()
                            .filter(r -> !r.contains("."))
                            .filter(r -> !DEFAULT_KEYCLOAK_ROLES.contains(r))
                            .toList();

                    List<String> permissions = allRoles.stream()
                            .filter(r -> r.contains("."))
                            .toList();

                    log.info("JWT validated - User: {} - Email: {} - ID: {}, Roles: {}, Permissions: {}" , username, email, userId, roles, permissions);

                    // Authorization check
                    if (requiresPermission(path, method)) {
                        String requiredPermission = getRequiredPermission(path, method);
                        if (!hasPermission(permissions, requiredPermission)) {
                            log.warn("Access denied - {} required: {} {}",
                                     requiredPermission, method, path);
                            return onError(exchange, requiredPermission + " required",
                                    HttpStatus.FORBIDDEN);
                        }
                    }

                    // Propaga header per i microservizi downstream
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-Username", username)
                            .header("X-User-Email", email)
                            .header("X-User-Roles", String.join(",", roles))
                            .header("X-User-Permissions", String.join(",", permissions))
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                // Se non c'è un principal (endpoint pubblici), lascia passare
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    // --- I tuoi metodi esistenti, invariati ---

    private boolean requiresPermission(String path, String method) {
        return getRequiredPermission(path, method) != null;
    }

    private String getRequiredPermission(String path, String method) {
        if (path.startsWith("/api/v1/users")) {
            return switch (method) {
                case "POST", "PUT", "FETCH" -> USER_WRITE;
                case "GET" -> USER_READ;
                case "DELETE" -> USER_DELETE;
                default -> null;
            };
        }
        if (path.startsWith("/api/v1/products")) {
            return switch (method) {
                case "POST", "PUT" -> PRODUCT_WRITE;
                case "DELETE" -> PRODUCT_DELETE;
                default -> null;
            };
        }
        if(path.startsWith("/api/v1/categories")){
            return switch (method) {
                case "POST", "PUT" -> CATEGORY_WRITE;
                case "GET" -> CATEGORY_READ;
                case "DELETE" -> CATEGORY_DELETE;
                default -> null;
            };
        }
        return null;
    }

    private boolean hasPermission(List<String> scopes, String requiredPermission) {
        return scopes != null && scopes.contains(requiredPermission);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        String errorResponse = String.format("{\"error\":\"%s\",\"status\":%d}",
                message, status.value());
        return response.writeWith(Mono.just(response.bufferFactory()
                .wrap(errorResponse.getBytes())));
    }
}
