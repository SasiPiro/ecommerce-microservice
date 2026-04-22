package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryResponseDto;
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
import com.ecommerce.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductMapper productMapper;
    @InjectMocks
    private ProductServiceImpl productService;

    // --- CONSTANTS ---
    private static final Long VALID_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final Long CAT_ID = 10L;
    private static final String DEFAULT_NAME = "Smartphone";

    // --- FACTORIES  ---
    private Category createCategoryEntity() {
        Category category = new Category("Electronics", "Desc");
        category.setId(CAT_ID);
        return category;
    }

    private Product createProductEntity() {
        Product product = new Product(DEFAULT_NAME, new BigDecimal("599.99"), 50, createCategoryEntity());
        product.setId(VALID_ID);
        return product;
    }

    private ProductRequestDto createRequest() {
        return new ProductRequestDto(DEFAULT_NAME, "Desc", new BigDecimal("599.99"), 50, CAT_ID);
    }

    private ProductResponseDto createResponse() {
        CategoryResponseDto catDto = new CategoryResponseDto(CAT_ID, "Electronics", "Desc");
        return new ProductResponseDto(VALID_ID, DEFAULT_NAME, "Desc", new BigDecimal("599.99"), 50, catDto);
    }

    // --- 1. CREATE ---

    @Test
    @DisplayName("Should create product successfully when valid request")
    void shouldCreateProductSuccessfully_whenValidRequest() {
        // GIVEN
        ProductRequestDto request = createRequest();
        Category category = createCategoryEntity();
        Product newProduct = createProductEntity();
        ProductResponseDto expectedResponse = createResponse();

        when(productRepository.existsByName(request.name())).thenReturn(false);
        when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.of(category));
        when(productMapper.toEntity(request)).thenReturn(newProduct);
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);
        when(productMapper.toResponseDTO(newProduct)).thenReturn(expectedResponse);

        // WHEN
        ProductResponseDto response = productService.createProduct(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(request.name());
        verify(productRepository).existsByName(request.name());
        verify(categoryRepository).findById(request.categoryId());
        verify(productRepository).save(newProduct);
    }

    @Test
    @DisplayName("Should throw ProductAlreadyExistsException when product name is already taken")
    void shouldThrowException_whenCreateNameAlreadyExists() {
        // GIVEN
        ProductRequestDto request = createRequest();
        when(productRepository.existsByName(request.name())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ProductAlreadyExistsException.class);

        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category indicated does not exist")
    void shouldThrowException_whenCreateCategoryNotFound() {
        // GIVEN
        ProductRequestDto request = createRequest();
        when(productRepository.existsByName(request.name())).thenReturn(false);
        when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    // --- 2. READ ---

    @Test
    @DisplayName("Should return list of all products")
    void shouldReturnAllProducts() {
        // GIVEN
        Product prod1 = createProductEntity();
        Product prod2 = createProductEntity();
        prod2.setId(2L);
        prod2.setName("Laptop");

        ProductResponseDto resp1 = createResponse();
        ProductResponseDto resp2 = new ProductResponseDto(2L, "Laptop", "Desc", new BigDecimal("1000"), 5, resp1.category());

        when(productRepository.findAll()).thenReturn(Arrays.asList(prod1, prod2));
        when(productMapper.toResponseDTO(prod1)).thenReturn(resp1);
        when(productMapper.toResponseDTO(prod2)).thenReturn(resp2);

        // WHEN
        List<ProductResponseDto> results = productService.getAllProducts();

        // THEN
        assertThat(results).hasSize(2)
                .extracting(ProductResponseDto::name)
                .containsExactlyInAnyOrder("Smartphone", "Laptop");
    }

    @Test
    @DisplayName("Should return product when searching by valid ID")
    void shouldReturnProduct_whenIdExists() {
        // GIVEN
        Product product = createProductEntity();
        when(productRepository.findById(VALID_ID)).thenReturn(Optional.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(createResponse());

        // WHEN
        ProductResponseDto response = productService.findById(VALID_ID);

        // THEN
        assertThat(response.id()).isEqualTo(VALID_ID);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when ID does not exist")
    void shouldThrowException_whenIdNotFound() {
        when(productRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(NON_EXISTENT_ID))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Should return products matching name keyword")
    void shouldReturnProducts_whenSearchByName() {
        Product product = createProductEntity();
        when(productRepository.findByNameContainingIgnoreCase("smart")).thenReturn(List.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(createResponse());

        List<ProductResponseDto> results = productService.searchByName("smart");

        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("Should return products in price range")
    void shouldReturnProducts_whenSearchByPriceRange() {
        Product product = createProductEntity();
        BigDecimal min = new BigDecimal("100");
        BigDecimal max = new BigDecimal("600");
        when(productRepository.findByPriceBetween(min, max)).thenReturn(List.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(createResponse());

        List<ProductResponseDto> results = productService.searchByPriceRange(min, max);

        assertThat(results).hasSize(1);
    }

    // --- 3. UPDATE (PUT) ---

    @Test
    @DisplayName("Should fully update product via PUT when valid request")
    void shouldUpdateProduct_whenValidRequest() {
        // GIVEN
        Product existingProduct = createProductEntity();
        ProductRequestDto request = new ProductRequestDto("NewPhone", "Desc", new BigDecimal("700"), 60, CAT_ID);
        Category category = createCategoryEntity();
        
        Product updatedProduct = new Product("NewPhone", new BigDecimal("700"), 60, category);
        updatedProduct.setId(VALID_ID);
        ProductResponseDto expectedResponse = new ProductResponseDto(VALID_ID, "NewPhone", "Desc", new BigDecimal("700"), 60, createResponse().category());

        when(productRepository.findById(VALID_ID)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByName("NewPhone")).thenReturn(false);
        when(categoryRepository.findById(CAT_ID)).thenReturn(Optional.of(category));
        
        when(productMapper.updateEntityFromDTO(request, existingProduct)).thenReturn(updatedProduct);
        when(productRepository.save(updatedProduct)).thenReturn(updatedProduct);
        when(productMapper.toResponseDTO(updatedProduct)).thenReturn(expectedResponse);

        // WHEN
        ProductResponseDto response = productService.updateProduct(VALID_ID, request);

        // THEN
        assertThat(response.name()).isEqualTo("NewPhone");
        verify(productRepository).save(updatedProduct);
    }

    @Test
    @DisplayName("Should skip uniqueness check when name matches current name")
    void shouldSkipNameCheck_whenUpdateNameMatchesCurrent() {
        Product existingProduct = createProductEntity();
        ProductRequestDto request = new ProductRequestDto(DEFAULT_NAME, "New desc", new BigDecimal("600"), 50, CAT_ID);
        Category category = createCategoryEntity();

        when(productRepository.findById(VALID_ID)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(CAT_ID)).thenReturn(Optional.of(category));
        
        // Mock the mapper simply returning the existing product modified
        when(productMapper.updateEntityFromDTO(request, existingProduct)).thenReturn(existingProduct);
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);
        when(productMapper.toResponseDTO(existingProduct)).thenReturn(createResponse());

        productService.updateProduct(VALID_ID, request);

        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository).save(existingProduct);
    }

    @Test
    @DisplayName("Should throw ProductAlreadyExistsException when update name is already associated")
    void shouldThrowException_whenUpdateNameAlreadyAssociated() {
        Product existingProduct = createProductEntity();
        ProductRequestDto request = new ProductRequestDto("TakenName", "Desc", new BigDecimal("100"), 10, CAT_ID);

        when(productRepository.findById(VALID_ID)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByName("TakenName")).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(VALID_ID, request))
                .isInstanceOf(ProductAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when new categoryId in PUT does not exist")
    void shouldThrowException_whenUpdateCategoryNotFound() {
        Product existingProduct = createProductEntity();
        ProductRequestDto request = new ProductRequestDto("NewName", "Desc", new BigDecimal("100"), 10, NON_EXISTENT_ID);

        when(productRepository.findById(VALID_ID)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByName("NewName")).thenReturn(false);
        when(categoryRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(VALID_ID, request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // --- 4. UPDATE (PATCH STOCK) ---

    @Test
    @DisplayName("Should partially update stock via PATCH and reload entity")
    void shouldPatchStock_whenValidRequest() {
        // GIVEN
        ProductStockRequestDto request = new ProductStockRequestDto(80);
        Product updatedProduct = createProductEntity();
        updatedProduct.setStock(80);
        ProductResponseDto expectedResponse = new ProductResponseDto(VALID_ID, DEFAULT_NAME, "Desc", new BigDecimal("599.99"), 80, createResponse().category());

        when(productRepository.existsById(VALID_ID)).thenReturn(true);
        when(productRepository.updateStock(VALID_ID, 80)).thenReturn(1);
        when(productRepository.findById(VALID_ID)).thenReturn(Optional.of(updatedProduct));
        when(productMapper.toResponseDTO(updatedProduct)).thenReturn(expectedResponse);

        // WHEN
        ProductResponseDto response = productService.patchStock(VALID_ID, request);

        // THEN
        assertThat(response.stock()).isEqualTo(80);
        verify(productRepository).updateStock(VALID_ID, 80);
        verify(productRepository).findById(VALID_ID); // Reload
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when patching stock for non-existent product")
    void shouldThrowException_whenPatchStockNonExistentProduct() {
        ProductStockRequestDto request = new ProductStockRequestDto(80);
        when(productRepository.existsById(NON_EXISTENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> productService.patchStock(NON_EXISTENT_ID, request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).updateStock(anyLong(), anyInt());
    }

    // --- 5. DELETE ---

    @Test
    @DisplayName("Should delete product successfully when product exists")
    void shouldDeleteProduct_whenProductExists() {
        when(productRepository.existsById(VALID_ID)).thenReturn(true);

        productService.deleteProduct(VALID_ID);

        verify(productRepository).existsById(VALID_ID);
        verify(productRepository).deleteById(VALID_ID);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
    void shouldThrowException_whenDeletingNonExistentProduct() {
        when(productRepository.existsById(NON_EXISTENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(NON_EXISTENT_ID))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(anyLong());
    }
}
