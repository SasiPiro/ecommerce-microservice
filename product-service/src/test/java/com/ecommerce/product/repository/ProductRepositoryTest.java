package com.ecommerce.product.repository;

import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = RepositoryConfig.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Electronics", "Electronic Devices");
        testCategory = entityManager.persistAndFlush(testCategory);
    }

    // --- FACTORY ---
    private Product createEntity() {
        return new Product("Smartphone", new BigDecimal("599.99"), 50, testCategory);
    }

    // --- TEST CREATE ---

    @Test
    @DisplayName("Should persist product with generated ID and audit fields")
    void shouldPersistProduct_withGeneratedId() {
        Product product = createEntity();

        Product savedProduct = productRepository.save(product);
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Smartphone");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("599.99");
        assertThat(savedProduct.getStock()).isEqualTo(50);
        assertThat(savedProduct.getCategory()).isEqualTo(testCategory);
        assertThat(savedProduct.getCreatedAt()).isNotNull();
        assertThat(savedProduct.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when name is duplicate")
    void shouldThrowException_whenNameIsDuplicate() {
        // GIVEN: existing product
        entityManager.persistAndFlush(createEntity());

        // same name of the first product
        Product product2 = new Product("Smartphone", new BigDecimal("699.99"), 10, testCategory);

        // WHEN AND THEN
        assertThatThrownBy(() -> productRepository.save(product2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- RICERCA PER ID ---

    @Test
    @DisplayName("Should return product when searching by existing ID")
    void shouldReturnProduct_whenIdExists() {
        // GIVEN
        Product product = createEntity();
        entityManager.persistAndFlush(product);

        // WHEN
        Optional<Product> opt = productRepository.findById(product.getId());

        // THEN
        assertThat(opt).isPresent();
        assertThat(opt.get().getName()).isEqualTo("Smartphone");
    }

    @Test
    @DisplayName("Should return empty Optional when ID does not exist")
    void shouldReturnEmpty_whenIdDoesNotExist() {
        // GIVEN
        Long idInesistente = 999L;

        // WHEN
        Optional<Product> opt = productRepository.findById(idInesistente);

        // THEN
        assertThat(opt).isEmpty();
    }

    // --- EXISTS BY NAME ---

    @Test
    @DisplayName("Should return true when name exists in database")
    void shouldReturnTrue_whenNameExists() {
        // GIVEN
        Product product = createEntity();
        entityManager.persistAndFlush(product);

        // WHEN
        boolean exists = productRepository.existsByName("Smartphone");

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when name does not exist in database")
    void shouldReturnFalse_whenNameDoesNotExist() {
        // WHEN
        boolean exists = productRepository.existsByName("Ghost Product");

        // THEN
        assertThat(exists).isFalse();
    }

    // --- FIND BY NAME CONTAINING ---

    @Test
    @DisplayName("Should return products when matching name keyword case-insensitive")
    void shouldReturnProducts_whenSearchingByNameKeyword() {
        // GIVEN
        entityManager.persist(createEntity());
        entityManager.persist(new Product("Smart TV", new BigDecimal("400.00"), 10, testCategory));
        entityManager.persist(new Product("Laptop", new BigDecimal("1000.00"), 5, testCategory));
        entityManager.flush();

        // WHEN
        List<Product> results = productRepository.findByNameContainingIgnoreCase("smart");

        // THEN
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getName)
                .containsExactlyInAnyOrder("Smartphone", "Smart TV");
    }

    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void shouldReturnEmptyList_whenNoProductsMatchKeyword() {
        // WHEN
        List<Product> results = productRepository.findByNameContainingIgnoreCase("Nothing");

        // THEN
        assertThat(results).isEmpty();
    }

    // --- FIND BY PRICE BETWEEN ---

    @Test
    @DisplayName("Should return products within price range")
    void shouldReturnProducts_whenSearchingByPriceRange() {
        // GIVEN
        entityManager.persist(createEntity()); // 599.99
        entityManager.persist(new Product("Cheap Phone", new BigDecimal("199.99"), 20, testCategory));
        entityManager.persist(new Product("Expensive Laptop", new BigDecimal("2000.00"), 5, testCategory));
        entityManager.flush();

        // WHEN
        List<Product> results = productRepository.findByPriceBetween(new BigDecimal("100.00"), new BigDecimal("600.00"));

        // THEN
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getName)
                .containsExactlyInAnyOrder("Smartphone", "Cheap Phone");
    }

    @Test
    @DisplayName("Should return empty list when no products match price range")
    void shouldReturnEmptyList_whenNoProductsMatchPriceRange() {
        // WHEN
        List<Product> results = productRepository.findByPriceBetween(new BigDecimal("10.00"), new BigDecimal("50.00"));

        // THEN
        assertThat(results).isEmpty();
    }

    // --- RECORD UPDATE (UPDATE STOCK) ---

    @Test
    @DisplayName("Should update stock and return updated rows count")
    void shouldUpdateStock_andReturnUpdatedRowsCount() {
        // GIVEN
        Product product = createEntity();
        product = entityManager.persistAndFlush(product);

        // WHEN
        Integer updatedRows = productRepository.updateStock(product.getId(), 85);
        entityManager.clear(); // Clear L1 cache to force reload from DB

        // THEN
        assertThat(updatedRows).isEqualTo(1);
        Product updatedProduct = entityManager.find(Product.class, product.getId());
        assertThat(updatedProduct.getStock()).isEqualTo(85);
    }

    // --- UPDATE ENTITY FIELDS ---

    @Test
    @DisplayName("Should update product fields and refresh updatedAt timestamp")
    void shouldUpdateProductFields_andRefreshUpdatedAtTimestamp() {
        // GIVEN
        Product product = createEntity();
        product = entityManager.persistAndFlush(product);
        LocalDateTime initialUpdateAt = product.getUpdatedAt();

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }

        // WHEN
        product.setPrice(new BigDecimal("499.99"));
        Product updatedProduct = productRepository.saveAndFlush(product);

        // THEN
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo("499.99");
        assertThat(updatedProduct.getUpdatedAt()).isAfter(initialUpdateAt); // Verifica @PreUpdate
    }

    // --- DELETE ---

    @Test
    @DisplayName("Should delete product and confirm removal from database")
    void shouldDeleteProduct_andConfirmRemoval() {
        // GIVEN
        Product product = createEntity();
        product = entityManager.persistAndFlush(product);

        // WHEN
        productRepository.delete(product);
        entityManager.flush();

        // THEN
        Product deleted = entityManager.find(Product.class, product.getId());
        assertThat(deleted).isNull();
    }
}
