package com.ecommerce.product.service.impl;

import com.ecommerce.product.dto.CategoryRequestDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.exception.CategoryAlreadyExistsException;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.ecommerce.product.constant.LogCode.*;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        log.info("Creating category - name: '{}'", dto.name());

        if (categoryRepository.existsByName(dto.name())) {
            log.warn("[{}] Creation rejected - category name '{}' already exists",
                    CATEGORY_NAME_ALREADY_EXISTS, dto.name());
            throw CategoryAlreadyExistsException.forName();
        }

        Category newCategory = categoryMapper.toEntity(dto);
        categoryRepository.save(newCategory);

        log.info("Category created successfully - id: {}, name: '{}'", newCategory.getId(), newCategory.getName());
        return categoryMapper.toResponseDto(newCategory);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDto> getAllCategories(Pageable pageable) {
        log.debug("Fetching categories with pagination - page: {}, size: {}", pageable.getPageNumber(),
                pageable.getPageSize());

        Page<CategoryResponseDto> result = categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponseDto);

        log.debug("Returned {} category(ies) on this page out of {} total", result.getNumberOfElements(),
                result.getTotalElements());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto findById(Long id) {
        log.debug("Looking up category by id: {}", id);
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponseDto)
                .orElseThrow(() -> {
                    log.warn("[{}] Category not found - id: {}", CATEGORY_NOT_FOUND, id);
                    return CategoryNotFoundException.forId();
                });
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
        log.info("Updating category - id: {}, new name: '{}'", id, dto.name());

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Update rejected - category not found - id: {}", CATEGORY_NOT_FOUND, id);
                    return CategoryNotFoundException.forId();
                });

        // Skip uniqueness check if the name hasn't changed (case-insensitive)
        if (!dto.name().equalsIgnoreCase(category.getName())) {
            if (categoryRepository.existsByName(dto.name())) {
                log.warn("[{}] Update rejected - category name '{}' already taken by another category",
                        CATEGORY_NAME_ALREADY_EXISTS, dto.name());
                throw CategoryAlreadyExistsException.forName();
            }
        }

        Category updatedCategory = categoryMapper.updateEntityFromDto(dto, category);
        categoryRepository.save(updatedCategory);

        log.info("Category updated successfully - id: {}", id);
        return categoryMapper.toResponseDto(updatedCategory);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category - id: {}", id);

        if (!categoryRepository.existsById(id)) {
            log.warn("[{}] Delete rejected - category not found - id: {}", CATEGORY_NOT_FOUND, id);
            throw CategoryNotFoundException.forId();
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully - id: {}", id);
    }
}
