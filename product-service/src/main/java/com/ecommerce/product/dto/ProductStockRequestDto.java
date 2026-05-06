package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductStockRequestDto(
                @NotNull(message = "New stock value required")
                @Min(0)
                @Schema(description = "Product stock", example = "10")
                Integer stock
) {}
