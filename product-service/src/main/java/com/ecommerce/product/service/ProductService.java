package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.dto.ProductStockRequestDto;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.exception.ProductAlreadyExistsException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for managing the lifecycle of {@link Product} entities.
 * <p>
 * This contract defines operations for creating, retrieving, updating, and deleting products.
 * It enforces business rules such as product name uniqueness and category existence.
 * </p>
 *
 * @author Salvatore Pirozzi
 * @version 1.0
 */
public interface ProductService {

    /**
     * Creates a new product in the catalog.
     * <p>
     * This method enforces uniqueness for the product name and validates
     * that the referenced category exists.
     * </p>
     *
     * @param dto The data transfer object containing the new product's information.
     * @return The persisted product mapped to a response DTO (including nested category).
     * @throws ProductAlreadyExistsException if the product name is already in use.
     * @throws CategoryNotFoundException     if the referenced category does not exist.
     */
    ProductResponseDto createProduct(ProductRequestDto dto);

    /**
     * Retrieves all products.
     * <p>
     * Returns the complete list of products in the catalog,
     * each including its nested category DTO.
     * </p>
     *
     * @return A list of {@link ProductResponseDto}.
     */
    List<ProductResponseDto> getAllProducts();

    /**
     * Retrieves a specific product by its unique identifier.
     *
     * @param id The unique ID of the product.
     * @return The requested product DTO (including nested category).
     * @throws ProductNotFoundException if no product is found with the provided ID.
     */
    ProductResponseDto findById(Long id);

    /**
     * Performs a full update of an existing product resource (PUT).
     * <p>
     * This method replaces the product's mutable data with the provided DTO.
     * It validates that the new name does not conflict with existing records
     * and that the referenced category exists.
     * </p>
     *
     * @param id  The ID of the product to update.
     * @param dto The DTO containing the new state of the product.
     * @return The updated product DTO (including nested category).
     * @throws ProductNotFoundException      if the product is not found.
     * @throws ProductAlreadyExistsException if the updated name is already in use by another product.
     * @throws CategoryNotFoundException     if the referenced category does not exist.
     */
    ProductResponseDto updateProduct(Long id, ProductRequestDto dto);

    /**
     * Applies a partial update to the stock of an existing product (PATCH).
     * <p>
     * Only the stock field is updated using an optimized JPQL query.
     * </p>
     *
     * @param id  The ID of the product to update.
     * @param dto The DTO containing the new stock value.
     * @return The updated product DTO (including nested category).
     * @throws ProductNotFoundException if the product is not found.
     */
    ProductResponseDto patchStock(Long id, ProductStockRequestDto dto);

    /**
     * Deletes a product from the system by its ID.
     * <p>
     * This operation checks for existence before deletion to ensure the ID is valid.
     * </p>
     *
     * @param id The unique ID of the product to delete.
     * @throws ProductNotFoundException if the product does not exist.
     */
    void deleteProduct(Long id);

    /**
     * Searches for products whose name contains the given keyword (case-insensitive).
     *
     * @param keyword The search term to match against product names.
     * @return A list of matching {@link ProductResponseDto}.
     */
    List<ProductResponseDto> searchByName(String keyword);

    /**
     * Searches for products within a given price range (inclusive).
     *
     * @param minPrice The minimum price boundary.
     * @param maxPrice The maximum price boundary.
     * @return A list of matching {@link ProductResponseDto}.
     */
    List<ProductResponseDto> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
}
