package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for full user replacement (PUT)")
public record UserPutRequestDTO(
                @Schema(description = "Unique username", example = "john_doe_updated") @NotBlank @Size(max = 50) String username,

                @Schema(description = "Valid email address", example = "john.new@example.com") @NotBlank @Size(max = 50) @Email(message = "Invalid email format") String email,

                @Schema(description = "New secure password", example = "N3wS3cur3P@ss!") @NotBlank @Size(min = 8, max = 100) String password,

                @Schema(description = "First name", example = "John") String firstName,

                @Schema(description = "Last name", example = "Doe") String lastName,

                @Schema(description = "Phone number", example = "+39 333 9999999") String phone,

                @Schema(description = "Account active status", example = "true") boolean active,

                @Schema(description = "User role", example = "SELLER") @NotNull User.UserRole userRole) {
}
