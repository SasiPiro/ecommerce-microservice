package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank(message = "Name required")
        @Size(max = 150)
        @Schema(description = "Product name", example = "Iphone")
        String name,

        @Schema(description = "Product description", example = "Mobile phone")
        String description,

        @NotNull(message = "Price required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be more than 0")
        @Schema(description = "Product price", example = "333.30")
        BigDecimal price,

        @NotNull(message = "Stock required")
        @Min(value = 0, message = "Stock can't be less than 0")
        @Schema(description = "Product stock", example = "10")
        Integer stock,

        @NotNull(message = "Category ID required")
        @Schema(description = "Category ID", example = "1")
        Long categoryId
) {}
