package com.ecommerce.user.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.ecommerce.user.model.Permission;
import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")  // Default: 24 ore
    private Long expiration;

    /**
     * Generate JWT token for user with roles and permissions
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        // User info
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());

        // Roles (lista nomi ruoli)
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        // Permissions (lista nomi permissions)
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .collect(Collectors.toList());

        // OAuth2 standard: scope separato da spazi
        claims.put("scope", String.join(" ", permissions));

        // Oppure array (più comune in JWT)
        claims.put("permissions", permissions);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Get expiration time in milliseconds
     */
    public Long getExpirationTime() {
        return expiration;
    }


    /**
     * Extract all claims from token
     */
    public Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract username from token
     */
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username",String.class);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractUserEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    // RBAC
    /**
     * Extract roles from token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    // RBAC + ABAC
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractAllClaims(token).get("permissions", List.class);
    }


    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
