package com.ecommerce.product.controller;

import com.ecommerce.product.dto.CategoryRequestDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController implements CategoryApiDoc {

    private final CategoryService categoryService;
    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto dto) {
        log.info("Creating category: {}", dto.name());
        CategoryResponseDto response = categoryService.createCategory(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    @GetMapping
    public Page<CategoryResponseDto> getAllCategories(@PageableDefault(sort = "id") Pageable pageable) {
        return categoryService.getAllCategories(pageable);
    }

    @Override
    @GetMapping("/{id}")
    public CategoryResponseDto getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @Override
    @PutMapping("/{id}")
    public CategoryResponseDto updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDto dto) {
        log.info("Updating category id: {}", id);
        return categoryService.updateCategory(id, dto);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        log.info("Deleting category id: {}", id);
        categoryService.deleteCategory(id);
    }
}
