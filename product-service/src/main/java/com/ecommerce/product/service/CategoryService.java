package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryRequestDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.exception.CategoryAlreadyExistsException;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.model.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing the lifecycle of {@link Category} entities.
 * <p>
 * This contract defines operations for creating, retrieving, updating, and
 * deleting categories.
 * It enforces business rules such as category name uniqueness.
 * </p>
 *
 * @author Salvatore Pirozzi
 * @version 1.0
 */
public interface CategoryService {

    /**
     * Creates a new category in the catalog.
     * <p>
     * This method enforces uniqueness for the category name.
     * </p>
     *
     * @param dto The data transfer object containing the new category's
     *            information.
     * @return The persisted category mapped to a response DTO.
     * @throws CategoryAlreadyExistsException if the name is already in use.
     */
    CategoryResponseDto createCategory(CategoryRequestDto dto);

    /**
     * Retrieves all categories as a paginated response.
     * <p>
     * Returns a page of categories. Supports pagination and sorting parameters.
     * </p>
     *
     * @param pageable pagination information
     * @return A Page of {@link CategoryResponseDto}.
     */
    Page<CategoryResponseDto> getAllCategories(Pageable pageable);

    /**
     * Retrieves a specific category by its unique identifier.
     *
     * @param id The unique ID of the category.
     * @return The requested category DTO.
     * @throws CategoryNotFoundException if no category is found with the provided
     *                                   ID.
     */
    CategoryResponseDto findById(Long id);

    /**
     * Performs a full update of an existing category resource.
     * <p>
     * This method replaces the category's mutable data with the provided DTO.
     * It validates that the new name does not conflict with existing records.
     * </p>
     *
     * @param id  The ID of the category to update.
     * @param dto The DTO containing the new state of the category.
     * @return The updated category DTO.
     * @throws CategoryNotFoundException      if the category is not found.
     * @throws CategoryAlreadyExistsException if the updated name is already in use
     *                                        by another category.
     */
    CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto);

    /**
     * Deletes a category from the system by its ID.
     * <p>
     * This operation checks for existence before deletion to ensure the ID is
     * valid.
     * </p>
     *
     * @param id The unique ID of the category to delete.
     * @throws CategoryNotFoundException if the category does not exist.
     */
    void deleteCategory(Long id);
}
