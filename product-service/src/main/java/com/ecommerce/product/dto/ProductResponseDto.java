package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductResponseDto(
        @Schema(description = "Product id", example = "1") Long id,
        @Schema(description = "Product name", example = "Iphone") String name,
        @Schema(description = "Product description", example = "Mobile phone") String description,
        @Schema(description = "Product price", example = "333.30") BigDecimal price,
        @Schema(description = "Product stock", example = "10") Integer stock,
        @Schema(description = "Category") CategoryResponseDto category
) {}
