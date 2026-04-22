package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryRequestDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.exception.CategoryAlreadyExistsException;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    // --- CONSTANTS ---
    private static final Long VALID_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final String DEFAULT_NAME = "Electronics";

    // --- FACTORIES (Maintainability) ---
    private Category createEntity() {
        Category category = new Category(DEFAULT_NAME, "Electronic Devices");
        category.setId(VALID_ID);
        return category;
    }

    private CategoryRequestDto createRequest() {
        return new CategoryRequestDto(DEFAULT_NAME, "Electronic Devices");
    }

    private CategoryResponseDto createResponse() {
        return new CategoryResponseDto(VALID_ID, DEFAULT_NAME, "Electronic Devices");
    }

    // --- 1. CREATE ---

    @Test
    @DisplayName("Should create category successfully when name is available")
    void shouldCreateCategorySuccessfully_whenNameIsAvailable() {
        // GIVEN
        CategoryRequestDto request = createRequest();
        Category newCategory = createEntity();
        CategoryResponseDto expectedResponse = createResponse();

        when(categoryRepository.existsByName(request.name())).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(newCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(categoryMapper.toResponseDto(newCategory)).thenReturn(expectedResponse);

        // WHEN
        CategoryResponseDto response = categoryService.createCategory(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(request.name());

        verify(categoryRepository).existsByName(request.name());
        verify(categoryMapper).toEntity(request);
        verify(categoryRepository).save(newCategory);
        verify(categoryMapper).toResponseDto(newCategory);
    }

    @Test
    @DisplayName("Should throw CategoryAlreadyExistsException when category name is already taken")
    void shouldThrowException_whenCreateNameAlreadyExists() {
        // GIVEN
        CategoryRequestDto request = createRequest();

        when(categoryRepository.existsByName(request.name())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(CategoryAlreadyExistsException.class);

        // VERIFY
        verify(categoryRepository).existsByName(request.name());
        verify(categoryMapper, never()).toEntity(any());
        verify(categoryRepository, never()).save(any());
    }

    // --- 2. READ ---

    @Test
    @DisplayName("Should return paginated list of categories")
    void shouldReturnPaginatedCategories_whenCategoriesExist() {
        // GIVEN
        Category category1 = createEntity();
        Category category2 = new Category("Books", "Books description");
        category2.setId(2L);
        CategoryResponseDto response1 = createResponse();
        CategoryResponseDto response2 = new CategoryResponseDto(2L, "Books", "Books description");

        Pageable pageable = PageRequest.of(0, 10);
        List<Category> entities = Arrays.asList(category1, category2);
        Page<Category> categoryPage = new PageImpl<>(entities, pageable, entities.size());

        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(categoryMapper.toResponseDto(category1)).thenReturn(response1);
        when(categoryMapper.toResponseDto(category2)).thenReturn(response2);

        // WHEN
        Page<CategoryResponseDto> result = categoryService.getAllCategories(pageable);

        // THEN
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(CategoryResponseDto::name)
                .containsExactlyInAnyOrder(response1.name(), response2.name());

        // VERIFY
        verify(categoryRepository).findAll(pageable);
        verify(categoryMapper, times(2)).toResponseDto(any(Category.class));
    }

    @Test
    @DisplayName("Should return category when searching by valid ID")
    void shouldReturnCategory_whenIdExists() {
        // GIVEN
        Category category = createEntity();

        when(categoryRepository.findById(VALID_ID)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponseDto(category)).thenReturn(createResponse());

        // WHEN
        CategoryResponseDto response = categoryService.findById(VALID_ID);

        // THEN
        assertThat(response.id()).isEqualTo(category.getId());
        assertThat(response.name()).isEqualTo(category.getName());
        verify(categoryRepository).findById(category.getId());
        verify(categoryMapper).toResponseDto(category);
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when ID does not exist")
    void shouldThrowException_whenIdNotFound() {
        // GIVEN
        when(categoryRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // WHEN
        assertThatThrownBy(() -> categoryService.findById(NON_EXISTENT_ID))
                .isInstanceOf(CategoryNotFoundException.class);

        // Verify Mapper never used
        verifyNoInteractions(categoryMapper);
    }

    // --- 3. UPDATE ---

    @Test
    @DisplayName("Should fully update category via PUT when valid request")
    void shouldUpdateCategory_whenValidRequest() {
        // GIVEN
        Category existingCategory = createEntity();
        CategoryRequestDto request = new CategoryRequestDto("NewName", "New Desc");
        Category updatedCategory = new Category("NewName", "New Desc");
        updatedCategory.setId(VALID_ID);
        CategoryResponseDto expectedResponse = new CategoryResponseDto(VALID_ID, "NewName", "New Desc");

        // mock repository find
        when(categoryRepository.findById(VALID_ID)).thenReturn(Optional.of(existingCategory));
        // mock uniqueness check (changed name)
        when(categoryRepository.existsByName("NewName")).thenReturn(false);
        // mock mapper update
        when(categoryMapper.updateEntityFromDto(request, existingCategory)).thenReturn(updatedCategory);
        when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);
        when(categoryMapper.toResponseDto(updatedCategory)).thenReturn(expectedResponse);

        // WHEN
        CategoryResponseDto response = categoryService.updateCategory(VALID_ID, request);

        // THEN
        assertThat(response.name()).isEqualTo("NewName");
        verify(categoryRepository).save(updatedCategory);
        verify(categoryMapper).toResponseDto(updatedCategory);
    }

    @Test
    @DisplayName("Should skip uniqueness check when name matches current name")
    void shouldSkipNameCheck_whenUpdateNameMatchesCurrent() {
        // GIVEN
        Category existingCategory = createEntity();
        // Request uses SAME name but different desc, or same name with different case
        CategoryRequestDto request = new CategoryRequestDto(DEFAULT_NAME.toUpperCase(), "New Desc");
        Category updatedCategory = new Category(DEFAULT_NAME, "New Desc");
        updatedCategory.setId(VALID_ID);
        CategoryResponseDto expectedResponse = new CategoryResponseDto(VALID_ID, DEFAULT_NAME, "New Desc");

        when(categoryRepository.findById(VALID_ID)).thenReturn(Optional.of(existingCategory));
        when(categoryMapper.updateEntityFromDto(request, existingCategory)).thenReturn(updatedCategory);
        when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);
        when(categoryMapper.toResponseDto(updatedCategory)).thenReturn(expectedResponse);

        // WHEN
        CategoryResponseDto response = categoryService.updateCategory(VALID_ID, request);

        // THEN
        assertThat(response.name()).isEqualTo(DEFAULT_NAME);
        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository).save(updatedCategory);
    }

    @Test
    @DisplayName("Should throw CategoryAlreadyExistsException when update name is already associated")
    void shouldThrowException_whenUpdateNameAlreadyAssociated() {
        // GIVEN
        Category existingCategory = createEntity();
        CategoryRequestDto request = new CategoryRequestDto("TakenName", "Desc");

        when(categoryRepository.findById(VALID_ID)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByName("TakenName")).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> categoryService.updateCategory(VALID_ID, request))
                .isInstanceOf(CategoryAlreadyExistsException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when updating non-existent category")
    void shouldThrowException_whenUpdatingNonExistentCategory() {
        // GIVEN
        CategoryRequestDto request = createRequest();
        when(categoryRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> categoryService.updateCategory(NON_EXISTENT_ID, request))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository, never()).save(any());
        verifyNoInteractions(categoryMapper);
    }

    // --- 4. DELETE ---

    @Test
    @DisplayName("Should delete category successfully when category exists")
    void shouldDeleteCategory_whenCategoryExists() {
        // GIVEN
        when(categoryRepository.existsById(VALID_ID)).thenReturn(true);

        // WHEN
        categoryService.deleteCategory(VALID_ID);

        // THEN
        verify(categoryRepository).existsById(VALID_ID);
        verify(categoryRepository).deleteById(VALID_ID);
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when deleting non-existent category")
    void shouldThrowException_whenDeletingNonExistentCategory() {
        // GIVEN
        when(categoryRepository.existsById(NON_EXISTENT_ID)).thenReturn(false);

        // WHEN
        assertThatThrownBy(() -> categoryService.deleteCategory(NON_EXISTENT_ID))
                .isInstanceOf(CategoryNotFoundException.class);

        // THEN
        verify(categoryRepository).existsById(NON_EXISTENT_ID);
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}
