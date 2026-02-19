package com.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank(message = "Name required")
        @Size(max = 150)
        String name,

        String description,

        @NotNull(message = "Preice required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be more than 0")
        BigDecimal price,

        @NotNull(message = "Stock required")
        @Min(value = 0, message = "Stock can't be less than 1")
        Integer stock,

        @NotNull(message = "Category ID required")
        Long categoryId
) {}
