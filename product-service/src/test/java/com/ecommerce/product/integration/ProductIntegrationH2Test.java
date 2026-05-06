package com.ecommerce.product.integration;

import com.ecommerce.product.ProductServiceApplication;
import com.ecommerce.product.constant.CategoryProductPermissionConstant;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ProductServiceApplication.class)
@ActiveProfiles("test")
@DisplayName("Integration Test (H2) - Full Lifecycle Category & Product")
class ProductIntegrationH2Test {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private String baseProductsUrl;
    private String baseCategoriesUrl;

    @Value("${test.server.host:localhost}")
    private String host;

    // HeaderAuthenticationFilter constants
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USERNAME = "X-Username";


    @BeforeEach
    void setUp() {
        // CLEAN SLATE
        restTemplate = new TestRestTemplate(new RestTemplateBuilder()
                .rootUri("http://" + host + ":" + port)
                .defaultHeader(HEADER_USER_ID, "999")
                .defaultHeader(HEADER_USERNAME, "admin_test")
                .defaultHeader(HEADER_USER_ROLES, "ADMIN,USER")
                .defaultHeader(HEADER_USER_PERMISSIONS, String.join(",",
                        CategoryProductPermissionConstant.CATEGORY_READ,
                        CategoryProductPermissionConstant.CATEGORY_WRITE,
                        CategoryProductPermissionConstant.CATEGORY_DELETE,
                        CategoryProductPermissionConstant.PRODUCT_READ,
                        CategoryProductPermissionConstant.PRODUCT_WRITE,
                        CategoryProductPermissionConstant.PRODUCT_DELETE)));

        baseProductsUrl = "/api/v1/products";
        baseCategoriesUrl = "/api/v1/categories";
        
        // Order is important because of Constraints
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("FULL LIFECYCLE - Create, Read, Update, Delete for Category and Product")
    void fullLifecycle_IntegrationFlow() {
        // --- STEP 1: CREATE CATEGORY ---
        CategoryRequestDto catReq = new CategoryRequestDto("CatIntTemp", "Desc");
        ResponseEntity<CategoryResponseDto> catRes = restTemplate.postForEntity(baseCategoriesUrl, catReq, CategoryResponseDto.class);
        assertThat(catRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long categoryId = Objects.requireNonNull(catRes.getBody()).id();

        // --- STEP 2: CREATE PRODUCT ---
        ProductRequestDto prodReq = new ProductRequestDto(
                "IPhone 15", "Apple Smartphone", new BigDecimal("999.00"), 100, categoryId
        );
        ResponseEntity<ProductResponseDto> prodRes = restTemplate.postForEntity(baseProductsUrl, prodReq, ProductResponseDto.class);
        assertThat(prodRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long productId = Objects.requireNonNull(prodRes.getBody()).id();
        assertThat(prodRes.getBody().name()).isEqualTo("IPhone 15");
        assertThat(prodRes.getBody().category().id()).isEqualTo(categoryId);

        // --- STEP 3: GET PRODUCT BY ID ---
        ResponseEntity<ProductResponseDto> getRes = restTemplate.getForEntity(baseProductsUrl + "/" + productId, ProductResponseDto.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getRes.getBody()).isNotNull();
        assertThat(getRes.getBody().stock()).isEqualTo(100);

        // --- STEP 4: PATCH STOCK ---
        ProductStockRequestDto stockReq = new ProductStockRequestDto(80);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductStockRequestDto> patchEntity = new HttpEntity<>(stockReq, headers);

        ResponseEntity<ProductResponseDto> patchRes = restTemplate.exchange(
                baseProductsUrl + "/" + productId + "/stock", HttpMethod.PATCH, patchEntity, ProductResponseDto.class);
        
        assertThat(patchRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchRes.getBody().stock()).isEqualTo(80);

        // --- STEP 5: FULL UPDATE PRODUCT (PUT) ---
        ProductRequestDto putReq = new ProductRequestDto(
                "IPhone 15 Pro", "Updated version", new BigDecimal("1199.00"), 50, categoryId
        );
        HttpEntity<ProductRequestDto> putEntity = new HttpEntity<>(putReq, headers);
        ResponseEntity<ProductResponseDto> putRes = restTemplate.exchange(
                baseProductsUrl + "/" + productId, HttpMethod.PUT, putEntity, ProductResponseDto.class);
        
        assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putRes.getBody().name()).isEqualTo("IPhone 15 Pro");
        assertThat(putRes.getBody().price()).isEqualByComparingTo("1199.00");

        // --- STEP 6: DELETE PRODUCT ---
        restTemplate.delete(baseProductsUrl + "/" + productId);
        
        // Verifica Delete (404)
        ResponseEntity<ProblemDetail> notFoundRes = restTemplate.getForEntity(baseProductsUrl + "/" + productId, ProblemDetail.class);
        assertThat(notFoundRes.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    
    @Test
    @DisplayName("Error - Should return 409 Conflict when category name already exists")
    void createCategory_DuplicateName_Returns409ProblemDetail() {
        // 1. GIVEN
        CategoryRequestDto request = new CategoryRequestDto("ConflictCat", "Desc");
        restTemplate.postForEntity(baseCategoriesUrl, request, CategoryResponseDto.class);

        // 2. WHEN
        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
                baseCategoriesUrl, request, ProblemDetail.class);

        // 3. ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Data conflict");
    }

    @Test
    @DisplayName("Error - Should return 400 and list of field errors when validation fails")
    void createProduct_InvalidData_Returns400WithErrorsMap() {
        // 1. GIVEN: Request invalida (stock negativo, price nullo)
        ProductRequestDto invalidRequest = new ProductRequestDto(
                "ValidName", "Desc", null, -10, 1L
        );

        // 2. WHEN
        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
                baseProductsUrl, invalidRequest, ProblemDetail.class);

        // 3. ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Invalid input data");

        // Verifichiamo la presenza degli errori
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.getProperties().get("errors");

        assertThat(errors).containsKeys("price", "stock");
    }

    @Test
    @DisplayName("Security Error - Should return 403 when user has read-only permission but tries to write")
    void createProduct_WithReadOnlyPermissions_Returns403Forbidden() {
        // 1. GIVEN: Un utente che ha SOLO il permesso di lettura
        TestRestTemplate readerTemplate = new TestRestTemplate(new RestTemplateBuilder()
                .rootUri("http://" + host + ":" + port)
                .defaultHeader("X-User-Id", "456")
                .defaultHeader("X-Username", "reader_user")
                .defaultHeader("X-User-Roles", "USER")
                .defaultHeader("X-User-Permissions", "product.read") // NON ha product.write
        );

        ProductRequestDto prodReq = new ProductRequestDto(
                "IPhone 15", "Apple Smartphone", new BigDecimal("999.00"), 100, 1L
        );

        // 2. WHEN: Prova a chiamare un endpoint che richiede @PreAuthorize("hasAuthority('user.write')")
        ResponseEntity<Object> response = readerTemplate.postForEntity(baseProductsUrl, prodReq, Object.class);

        // 3. ASSERT: Deve tornare 403 Forbidden
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Security - Should skip authentication if headers are missing")
    void missingHeaders_ShouldNotAuthenticate() {
        // RestTemplate without header
        TestRestTemplate anonymousTemplate = new TestRestTemplate(new RestTemplateBuilder()
                .rootUri("http://" + host + ":" + port));

        ResponseEntity<Object> response = anonymousTemplate.getForEntity(baseProductsUrl, Object.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED);
    }
}
