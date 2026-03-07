package com.ecommerce.product.mapper;

import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public ProductMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public ProductResponseDto toResponseDTO(Product product) {
        if (product == null)
            return null;

        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                categoryMapper.toResponseDto(product.getCategory()) // Mapping annidato
        );
    }

    public Product toEntity(ProductRequestDto dto) {
        if (dto == null)
            return null;

        Product product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        return product;
    }

    public Product updateEntityFromDTO(ProductRequestDto dto, Product product) {
        if (dto == null || product == null)
            return product;

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        // Nota: Il cambio di categoria verrà gestito dalla logica del Service
        return product;
    }
}
