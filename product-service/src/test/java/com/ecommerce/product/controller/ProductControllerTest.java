package com.ecommerce.product.controller;

import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.dto.ProductStockRequestDto;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.exception.ProductAlreadyExistsException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- CONSTANTS ---
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 999L;
    private static final String DEFAULT_NAME = "Smartphone";
    private static final String DEFAULT_DESC = "Electronic device";
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("599.99");
    private static final Integer DEFAULT_STOCK = 50;
    private static final Long CAT_ID = 10L;

    // Paths
    private static final String BASE_PATH = "/api/v1/products";
    private static final String ID_PATH = BASE_PATH + "/{id}";
    private static final String SEARCH_PATH = BASE_PATH + "/search";
    private static final String PRICE_RANGE_PATH = BASE_PATH + "/price-range";

    // Factory per Request
    private ProductRequestDto createRequest() {
        return new ProductRequestDto(DEFAULT_NAME, DEFAULT_DESC, DEFAULT_PRICE, DEFAULT_STOCK, CAT_ID);
    }

    // Factory per Response
    private ProductResponseDto createResponse() {
        CategoryResponseDto catDto = new CategoryResponseDto(CAT_ID, "Electronics", "Desc");
        return new ProductResponseDto(VALID_ID, DEFAULT_NAME, DEFAULT_DESC, DEFAULT_PRICE, DEFAULT_STOCK, catDto);
    }

    // --- 1. CREATE (POST) ---

    @Test
    @DisplayName("POST /api/v1/products - Should return 201 Created and Location Header")
    void createProduct_WithValidData_ReturnsCreated() throws Exception {
        when(productService.createProduct(any(ProductRequestDto.class))).thenReturn(createResponse());

        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE_PATH + "/" + VALID_ID)))
                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                .andExpect(jsonPath("$.name", is(DEFAULT_NAME)));

        verify(productService).createProduct(any(ProductRequestDto.class));
    }

    // --- POST CREATE ERRORS ---

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 Bad Request when Validation Fails")
    void createProduct_WithInvalidDTO_ReturnsBadRequest() throws Exception {
        // Given: invalid stock (< 0) and null price
        ProductRequestDto invalidDto = new ProductRequestDto("Name", "Desc", null, -5, CAT_ID);

        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); // 400

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 409 Conflict when Product name already exists")
    void createProduct_WhenNameExists_ReturnsConflict() throws Exception {
        when(productService.createProduct(any(ProductRequestDto.class)))
                .thenThrow(ProductAlreadyExistsException.forName());

        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict()); // 409
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 404 Not Found when Category does not exist")
    void createProduct_WhenCategoryNotFound_ReturnsNotFound() throws Exception {
        when(productService.createProduct(any(ProductRequestDto.class)))
                .thenThrow(CategoryNotFoundException.forId());

        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isNotFound()); // 404
    }

    // --- 2. READ (GET) ---

    @Test
    @DisplayName("GET /api/v1/products - Should return 200 OK and list of all products")
    void getAllProducts_ReturnsList() throws Exception {
        List<ProductResponseDto> productList = List.of(
                createResponse(),
                new ProductResponseDto(2L, "Laptop", "High end", new BigDecimal("1200"), 10, null)
        );

        when(productService.getAllProducts()).thenReturn(productList);

        mockMvc.perform(get(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(DEFAULT_NAME)))
                .andExpect(jsonPath("$[1].name", is("Laptop")));

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return 200 OK when found")
    void getById_WhenProductExists_ReturnsProduct() throws Exception {
        when(productService.findById(VALID_ID)).thenReturn(createResponse());

        mockMvc.perform(get(ID_PATH, VALID_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                .andExpect(jsonPath("$.name", is(DEFAULT_NAME)));

        verify(productService).findById(VALID_ID);
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Should return 200 OK with query param")
    void searchByName_WhenProductExists_ReturnsList() throws Exception {
        when(productService.searchByName(DEFAULT_NAME)).thenReturn(List.of(createResponse()));

        mockMvc.perform(get(SEARCH_PATH)
                .param("keyword", DEFAULT_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(DEFAULT_NAME)));

        verify(productService).searchByName(DEFAULT_NAME);
    }

    @Test
    @DisplayName("GET /api/v1/products/price-range - Should return 200 OK with min and max params")
    void searchByPriceRange_ReturnsList() throws Exception {
        BigDecimal min = new BigDecimal("100");
        BigDecimal max = new BigDecimal("1000");

        when(productService.searchByPriceRange(min, max)).thenReturn(List.of(createResponse()));

        mockMvc.perform(get(PRICE_RANGE_PATH)
                .param("min", "100")
                .param("max", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(productService).searchByPriceRange(min, max);
    }

    // --- GET READ ERRORS ---

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return 404 Not Found when ID does not exist")
    void getById_WhenProductNotFound_ReturnsNotFound() throws Exception {
        when(productService.findById(INVALID_ID))
                .thenThrow(ProductNotFoundException.forId());

        mockMvc.perform(get(ID_PATH, INVALID_ID))
                .andExpect(status().isNotFound()); // 404
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Should return 400 when keyword parameter is missing")
    void searchByName_WithoutParam_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get(SEARCH_PATH))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    @DisplayName("GET /api/v1/products/price-range - Should return 400 when range parameters are missing")
    void searchByPriceRange_WithoutParam_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get(PRICE_RANGE_PATH)
                .param("min", "100"))
                .andExpect(status().isBadRequest()); // 400 missing "max"
    }

    // --- 3. UPDATE PUT ---

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should return 200 OK and updated DTO")
    void updateFull_WithValidData_ReturnsUpdatedProduct() throws Exception {
        when(productService.updateProduct(eq(VALID_ID), any(ProductRequestDto.class))).thenReturn(createResponse());

        mockMvc.perform(put(ID_PATH, VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                .andExpect(jsonPath("$.name", is(DEFAULT_NAME)));

        verify(productService).updateProduct(eq(VALID_ID), any(ProductRequestDto.class));
    }

    // --- 3. PUT UPDATE ERRORS ---

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should return 400 Bad Request when Validation Fails")
    void updateFull_WithInvalidDTO_ReturnsBadRequest() throws Exception {
        // Given: missing categoryId
        ProductRequestDto invalidDto = new ProductRequestDto("Name", "Desc", new BigDecimal("100"), 10, null);

        mockMvc.perform(put(ID_PATH, VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should return 404 Not Found when updating non-existent product")
    void updateFull_WhenProductNotFound_ReturnsNotFound() throws Exception {
        when(productService.updateProduct(eq(INVALID_ID), any(ProductRequestDto.class)))
                .thenThrow(ProductNotFoundException.forId());

        mockMvc.perform(put(ID_PATH, INVALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should return 409 Conflict on duplicate name")
    void updateFull_WhenNameTaken_ReturnsConflict() throws Exception {
        when(productService.updateProduct(eq(VALID_ID), any(ProductRequestDto.class)))
                .thenThrow(ProductAlreadyExistsException.forName());

        mockMvc.perform(put(ID_PATH, VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict());
    }

    // --- 4. UPDATE PATCH (STOCK) ---

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/stock - Should return 200 OK")
    void patchStock_WithValidData_ReturnsUpdatedProduct() throws Exception {
        Integer expectedStock = 85;
        ProductStockRequestDto patchDto = new ProductStockRequestDto(expectedStock);
        
        ProductResponseDto patchResponse = new ProductResponseDto(
                VALID_ID, DEFAULT_NAME, DEFAULT_DESC, DEFAULT_PRICE, expectedStock, null);

        when(productService.patchStock(eq(VALID_ID), any(ProductStockRequestDto.class))).thenReturn(patchResponse);

        mockMvc.perform(patch(ID_PATH + "/stock", VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(expectedStock)));

        verify(productService).patchStock(eq(VALID_ID), any(ProductStockRequestDto.class));
    }

    // --- 4. UPDATE PATCH ERRORS ---

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/stock - Should return 400 Bad Request when Validation Fails")
    void patchStock_WithInvalidData_ReturnsBadRequest() throws Exception {
        // Negative stock
        ProductStockRequestDto patchDto = new ProductStockRequestDto(-1);

        mockMvc.perform(patch(ID_PATH + "/stock", VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).patchStock(any(), any());
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/stock - Should return 404 Not Found")
    void patchStock_WhenProductNotFound_ReturnsNotFound() throws Exception {
        ProductStockRequestDto patchDto = new ProductStockRequestDto(100);

        when(productService.patchStock(eq(INVALID_ID), any(ProductStockRequestDto.class)))
                .thenThrow(ProductNotFoundException.forId());

        mockMvc.perform(patch(ID_PATH + "/stock", INVALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isNotFound());
    }

    // --- 5. DELETE ---

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should return 204 No Content")
    void delete_WhenProductExists_ReturnsNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(VALID_ID);

        mockMvc.perform(delete(ID_PATH, VALID_ID))
                .andExpect(status().isNoContent()); // 204 No Content

        verify(productService).deleteProduct(VALID_ID);
    }

    // --- 5. DELETE ERRORS ---

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should return 404 Not Found if product doesn't exist")
    void delete_WhenProductNotFound_ReturnsNotFound() throws Exception {
        doThrow(ProductNotFoundException.forId()).when(productService).deleteProduct(INVALID_ID);

        mockMvc.perform(delete(ID_PATH, INVALID_ID))
                .andExpect(status().isNotFound()); // 404

        verify(productService).deleteProduct(INVALID_ID);
    }
}
