package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for partial user update (PATCH) - only include fields to change")
public record UserPatchRequestDTO(
                @Schema(description = "First name", example = "John") @Size(min = 2, max = 50) String firstName,

                @Schema(description = "Last name", example = "Doe") @Size(min = 2, max = 50) String lastName,

                @Schema(description = "New email address", example = "john.patched@example.com") @Email(message = "Invalid email format") String email,

                @Schema(description = "Phone number", example = "+39 333 0000001") String phone) {
}
