package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryResponseDto(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "Category name", example = "Electronics") String name,
        @Schema(description = "Category description", example = "Electronics devices") String description
) {}
