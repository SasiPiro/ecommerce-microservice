package com.ecommerce.product.mapper;

import com.ecommerce.product.dto.CategoryRequestDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDto toResponseDto(Category category) {
        if (category == null) return null;
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription());
    }

    public Category toEntity(CategoryRequestDto dto) {
        if (dto == null) return null;
        Category category = new Category();
        category.setName(dto.name());
        category.setDescription(dto.description());
        return category;
    }

    public Category updateEntityFromDto(CategoryRequestDto dto, Category category) {
        if (dto == null || category == null) return category;
        category.setName(dto.name());
        category.setDescription(dto.description());
        return category;
    }
}
