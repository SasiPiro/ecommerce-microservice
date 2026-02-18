package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for user registration")
public record UserRequestDTO(
                @Schema(description = "Unique username", example = "john_doe") @NotBlank(message = "Required Username") @Size(min = 3, max = 50) String username,

                @Schema(description = "Valid email address", example = "john.doe@example.com") @NotBlank(message = "Required Email") @Email(message = "Invalid email format") String email,

                @Schema(description = "Secure password", example = "S3cur3P@ss!") @NotBlank(message = "Required Password") @Size(min = 6, max = 100) String password,

                @Schema(description = "First name", example = "John") @Size(min = 2, max = 50) String firstName,

                @Schema(description = "Last name", example = "Doe") @Size(min = 2, max = 50) String lastName,

                @Schema(description = "Phone number", example = "+39 333 1234567") String phone) {
}
