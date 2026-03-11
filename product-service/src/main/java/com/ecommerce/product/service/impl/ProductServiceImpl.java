package com.ecommerce.product.service.impl;

import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.dto.ProductStockRequestDto;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.exception.ProductAlreadyExistsException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.ecommerce.product.constant.LogCode.*;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        log.info("Creating product - name: '{}', categoryId: {}", dto.name(), dto.categoryId());

        if (productRepository.existsByName(dto.name())) {
            log.warn("[{}] Creation rejected - product name '{}' already exists",
                    PRODUCT_NAME_ALREADY_EXISTS, dto.name());
            throw ProductAlreadyExistsException.forName();
        }

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> {
                    log.warn("[{}] Creation rejected - category not found - id: {}", CATEGORY_NOT_FOUND, dto.categoryId());
                    return CategoryNotFoundException.forId();
                });

        Product newProduct = productMapper.toEntity(dto);
        newProduct.setCategory(category);
        productRepository.save(newProduct);

        log.info("Product created successfully - id: {}, name: '{}'", newProduct.getId(), newProduct.getName());
        return productMapper.toResponseDTO(newProduct);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        log.debug("Fetching all products");

        List<ProductResponseDto> result = productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDTO)
                .toList();

        log.debug("Returned {} product(s)", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto findById(Long id) {
        log.debug("Looking up product by id: {}", id);
        return productRepository.findById(id)
                .map(productMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("[{}] Product not found - id: {}", PRODUCT_NOT_FOUND, id);
                    return ProductNotFoundException.forId();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> searchByName(String keyword) {
        log.debug("Searching products by name keyword: '{}'", keyword);

        List<ProductResponseDto> result = productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(productMapper::toResponseDTO)
                .toList();

        log.debug("Found {} product(s) matching keyword '{}'", result.size(), keyword);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Searching products by price range: {} - {}", minPrice, maxPrice);

        List<ProductResponseDto> result = productRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(productMapper::toResponseDTO)
                .toList();

        log.debug("Found {} product(s) in price range {} - {}", result.size(), minPrice, maxPrice);
        return result;
    }

    // -------------------------------------------------------------------------
    // UPDATE - full replacement (PUT)
    // -------------------------------------------------------------------------

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {
        log.info("Full update (PUT) - product id: {}, new name: '{}', new categoryId: {}",
                id, dto.name(), dto.categoryId());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] PUT rejected - product not found - id: {}", PRODUCT_NOT_FOUND, id);
                    return ProductNotFoundException.forId();
                });

        // Skip uniqueness check if the name hasn't changed (case-insensitive)
        if (!dto.name().equalsIgnoreCase(product.getName())) {
            if (productRepository.existsByName(dto.name())) {
                log.warn("[{}] PUT rejected - product name '{}' already taken by another product",
                        PRODUCT_NAME_ALREADY_EXISTS, dto.name());
                throw ProductAlreadyExistsException.forName();
            }
        }

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> {
                    log.warn("[{}] PUT rejected - category not found - id: {}", CATEGORY_NOT_FOUND, dto.categoryId());
                    return CategoryNotFoundException.forId();
                });

        Product updatedProduct = productMapper.updateEntityFromDTO(dto, product);
        updatedProduct.setCategory(category);
        productRepository.save(updatedProduct);

        log.info("Product updated successfully - id: {}", id);
        return productMapper.toResponseDTO(updatedProduct);
    }

    // -------------------------------------------------------------------------
    // UPDATE - partial stock (PATCH)
    // -------------------------------------------------------------------------

    @Override
    public ProductResponseDto patchStock(Long id, ProductStockRequestDto dto) {
        log.info("Patching stock - product id: {}, new stock: {}", id, dto.stock());

        if (!productRepository.existsById(id)) {
            log.warn("[{}] Patch rejected - product not found - id: {}", PRODUCT_NOT_FOUND, id);
            throw ProductNotFoundException.forId();
        }

        productRepository.updateStock(id, dto.stock());

        // Reload entity to return fresh DTO with updated stock
        Product updated = productRepository.findById(id).orElseThrow(ProductNotFoundException::forId);

        log.info("Product stock updated successfully - id: {}, stock: {}", id, dto.stock());
        return productMapper.toResponseDTO(updated);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product - id: {}", id);

        if (!productRepository.existsById(id)) {
            log.warn("[{}] Delete rejected - product not found - id: {}", PRODUCT_NOT_FOUND, id);
            throw ProductNotFoundException.forId();
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully - id: {}", id);
    }
}
