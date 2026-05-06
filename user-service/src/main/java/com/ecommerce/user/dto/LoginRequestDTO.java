package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @Schema(description = "Unique username or email", example = "admin") @NotBlank(message = "Username or email is required") @Size(min = 3, max = 50) String usernameOrEmail,

        @Schema(description = "Secure password", example = "admin") @NotBlank(message = "Required Password") @Size(min = 4, max = 100) String password
        ) {
}
