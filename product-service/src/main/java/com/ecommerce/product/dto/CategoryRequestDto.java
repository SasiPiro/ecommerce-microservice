package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
        @NotBlank(message = "Category name required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        @Schema(description = "Category name", example = "Electronics")
        String name,

        @Size(max = 500, message = "Description must be under 500 characters")
        @Schema(description = "Category description", example = "Electronics devices")
        String description
) {}
