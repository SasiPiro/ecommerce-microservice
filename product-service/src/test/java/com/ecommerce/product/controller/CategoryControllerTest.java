package com.ecommerce.product.controller;

import com.ecommerce.product.dto.CategoryRequestDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.exception.CategoryAlreadyExistsException;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- CONSTANTS ---
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 999L;
    private static final String DEFAULT_NAME = "Electronics";
    private static final String DEFAULT_DESC = "Electronic devices";

    // Paths
    private static final String BASE_PATH = "/api/v1/categories";
    private static final String ID_PATH = BASE_PATH + "/{id}";

    // Factory per Request
    private CategoryRequestDto createRequest() {
        return new CategoryRequestDto(DEFAULT_NAME, DEFAULT_DESC);
    }

    // Factory per Response
    private CategoryResponseDto createResponse() {
        return new CategoryResponseDto(VALID_ID, DEFAULT_NAME, DEFAULT_DESC);
    }

    // --- 1. CREATE (POST) ---

    @Test
    @DisplayName("POST /api/v1/categories - Should return 201 Created and Location Header")
    void createCategory_WithValidData_ReturnsCreated() throws Exception {
        when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(createResponse());

        // When & Then
        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE_PATH + "/" + VALID_ID)))
                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                .andExpect(jsonPath("$.name", is(DEFAULT_NAME)))
                .andExpect(jsonPath("$.description", is(DEFAULT_DESC)));

        verify(categoryService).createCategory(any(CategoryRequestDto.class));
    }

    // --- POST CREATE ERRORS ---

    @Test
    @DisplayName("POST /api/v1/categories - Should return 400 Bad Request when Validation Fails")
    void createCategory_WithInvalidDTO_ReturnsBadRequest() throws Exception {
        // Given: empty name to trigger @Valid @NotBlank
        CategoryRequestDto invalidDto = new CategoryRequestDto("", "Desc");

        // When & Then
        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); // 400

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should return 409 Conflict when Category already exists")
    void createCategory_WhenNameExists_ReturnsConflict() throws Exception {
        when(categoryService.createCategory(any(CategoryRequestDto.class)))
                .thenThrow(CategoryAlreadyExistsException.forName());

        // When & Then
        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict()); // 409
    }

    // --- 2. READ (GET) ---

    @Test
    @DisplayName("GET /api/v1/categories - Should return 200 OK and list of categories")
    void getAllCategories_ReturnsPage() throws Exception {
        // Given
        List<CategoryResponseDto> categoryList = List.of(
                createResponse(),
                new CategoryResponseDto(2L, "Books", "Books and media"));

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<CategoryResponseDto> categoryPage = new PageImpl<>(categoryList, pageRequest, categoryList.size());

        when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(categoryPage);

        // When & Then
        mockMvc.perform(get(BASE_PATH)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is(DEFAULT_NAME)))
                .andExpect(jsonPath("$.content[1].name", is("Books")))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)));

        verify(categoryService).getAllCategories(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return 200 OK when found")
    void getById_WhenCategoryExists_ReturnsCategory() throws Exception {
        when(categoryService.findById(VALID_ID)).thenReturn(createResponse());

        // When & Then
        mockMvc.perform(get(ID_PATH, VALID_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                .andExpect(jsonPath("$.name", is(DEFAULT_NAME)))
                .andExpect(jsonPath("$.description", is(DEFAULT_DESC)));

        verify(categoryService).findById(VALID_ID);
    }

    // --- GET READ ERRORS ---

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return 404 Not Found when ID does not exist")
    void getById_WhenCategoryNotFound_ReturnsNotFound() throws Exception {
        when(categoryService.findById(INVALID_ID))
                .thenThrow(CategoryNotFoundException.forId());

        // When & Then
        mockMvc.perform(get(ID_PATH, INVALID_ID))
                .andExpect(status().isNotFound()); // 404
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return 400 Bad Request for invalid ID format")
    void getById_WithInvalidIdType_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_PATH + "/abc"))
                .andExpect(status().isBadRequest()); // 400 Type Mismatch
    }

    // --- 3. UPDATE PUT ---

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should return 200 OK and updated DTO")
    void updateFull_WithValidData_ReturnsUpdatedCategory() throws Exception {
        when(categoryService.updateCategory(eq(VALID_ID), any(CategoryRequestDto.class))).thenReturn(createResponse());

        // When & Then
        mockMvc.perform(put(ID_PATH, VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                .andExpect(jsonPath("$.name", is(DEFAULT_NAME)))
                .andExpect(jsonPath("$.description", is(DEFAULT_DESC)));

        verify(categoryService).updateCategory(eq(VALID_ID), any(CategoryRequestDto.class));
    }

    // --- 3. PUT UPDATE ERRORS ---

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should return 400 Bad Request when Validation Fails")
    void updateFull_WithInvalidDTO_ReturnsBadRequest() throws Exception {
        // Given: short name
        CategoryRequestDto invalidDto = new CategoryRequestDto("a", "desc");

        // When & Then
        mockMvc.perform(put(ID_PATH, VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategory(any(), any());
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should return 404 Not Found when updating non-existent category")
    void updateFull_WhenCategoryNotFound_ReturnsNotFound() throws Exception {
        when(categoryService.updateCategory(eq(INVALID_ID), any(CategoryRequestDto.class)))
                .thenThrow(CategoryNotFoundException.forId());

        // When & Then
        mockMvc.perform(put(ID_PATH, INVALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should return 409 Conflict on duplicate name")
    void updateFull_WhenNameTaken_ReturnsConflict() throws Exception {
        when(categoryService.updateCategory(eq(VALID_ID), any(CategoryRequestDto.class)))
                .thenThrow(CategoryAlreadyExistsException.forName());

        // When & Then
        mockMvc.perform(put(ID_PATH, VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict());
    }

    // --- 4. DELETE ---

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 204 No Content")
    void delete_WhenCategoryExists_ReturnsNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(VALID_ID);

        // When & Then
        mockMvc.perform(delete(ID_PATH, VALID_ID))
                .andExpect(status().isNoContent()); // 204 No Content

        verify(categoryService).deleteCategory(VALID_ID);
    }

    // --- 4. DELETE ERRORS ---

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 404 Not Found if category doesn't exist")
    void delete_WhenCategoryNotFound_ReturnsNotFound() throws Exception {
        doThrow(CategoryNotFoundException.forId()).when(categoryService).deleteCategory(INVALID_ID);

        // When & Then
        mockMvc.perform(delete(ID_PATH, INVALID_ID))
                .andExpect(status().isNotFound()); // 404

        verify(categoryService).deleteCategory(INVALID_ID);
    }
}
