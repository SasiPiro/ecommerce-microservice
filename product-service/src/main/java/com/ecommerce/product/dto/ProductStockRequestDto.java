package com.ecommerce.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductStockRequestDto(
        @NotNull(message = "New stock value required")
        @Min(0)
        Integer stock
) {}
