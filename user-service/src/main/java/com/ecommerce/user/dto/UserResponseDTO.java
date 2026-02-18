package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "User data returned after creation or lookup")
public record UserResponseDTO(
                @Schema(description = "Unique user identifier", example = "42") Long id,

                @Schema(description = "Username", example = "john_doe") String username,

                @Schema(description = "Email address", example = "john.doe@example.com") String email,

                @Schema(description = "First name", example = "John") String firstName,

                @Schema(description = "Last name", example = "Doe") String lastName,

                @Schema(description = "Phone number", example = "+39 333 1234567") String phone,

                @Schema(description = "Assigned role", example = "CUSTOMER") User.UserRole userRole,

                @Schema(description = "Account creation timestamp", example = "2025-01-15T10:30:00") LocalDateTime createdAt) {
}
