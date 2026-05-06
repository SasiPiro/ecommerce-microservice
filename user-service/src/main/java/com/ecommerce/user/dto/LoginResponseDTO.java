package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponseDTO(
        @Schema(description = "Jwt Token", example = "feufh37d8ufiuf383lca0w") String token,

        @Schema(description = "Type", example = "Bearer") String type ,

        @Schema(description = "Expiring time", example = "100000000") Long expiresIn,

        @Schema(description = "User object") UserResponseDTO user
) {
    public LoginResponseDTO(String token, Long expiresIn, UserResponseDTO user) {
        // Chiama il costruttore principale inserendo "Bearer" di default
        this(token, "Bearer", expiresIn, user);
    }
}
