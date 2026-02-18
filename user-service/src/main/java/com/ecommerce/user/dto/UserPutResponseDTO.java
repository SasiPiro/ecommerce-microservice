package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "User data returned after a full update (PUT)")
public record UserPutResponseDTO(
                @Schema(description = "Unique user identifier", example = "42") Long id,

                @Schema(description = "Username", example = "john_doe_updated") String username,

                @Schema(description = "Email address", example = "john.new@example.com") String email,

                @Schema(description = "First name", example = "John") String firstName,

                @Schema(description = "Last name", example = "Doe") String lastName,

                @Schema(description = "Phone number", example = "+39 333 9999999") String phone,

                @Schema(description = "Account active status", example = "true") boolean active,

                @Schema(description = "User role", example = "SELLER") User.UserRole userRole,

                @Schema(description = "Account creation timestamp", example = "2025-01-15T10:30:00") LocalDateTime createdAt,

                @Schema(description = "Last update timestamp", example = "2025-06-01T08:00:00") LocalDateTime updatedAt) {
}
