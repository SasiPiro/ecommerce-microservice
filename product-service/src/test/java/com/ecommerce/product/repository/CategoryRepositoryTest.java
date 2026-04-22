package com.ecommerce.product.repository;

import com.ecommerce.product.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = RepositoryConfig.class)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- FACTORY ---
    private Category createEntity() {
        return new Category("Electronics", "Category for electronic devices");
    }

    // --- TEST CREATE ---

    @Test
    @DisplayName("Should persist category with generated ID and audit fields")
    void shouldPersistCategory_withGeneratedIdAndAuditFields() {
        Category category = createEntity();

        Category savedCategory = categoryRepository.save(category);
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Electronics");
        assertThat(savedCategory.getDescription()).isEqualTo("Category for electronic devices");
        assertThat(savedCategory.getCreatedAt()).isNotNull(); // Gestito da costruttore
        assertThat(savedCategory.getUpdatedAt()).isNotNull(); // Gestito da costruttore
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when name is duplicate in insertion")
    void shouldThrowException_whenNameIsDuplicate() {
        // GIVEN: existing category
        entityManager.persistAndFlush(createEntity());

        // same name of the first category
        Category category2 = new Category("Electronics", "Another description");

        // WHEN AND THEN
        assertThatThrownBy(() -> categoryRepository.save(category2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- RICERCA PER ID ---

    @Test
    @DisplayName("Should return category when searching by existing ID")
    void shouldReturnCategory_whenIdExists() {
        // GIVEN
        Category category = createEntity();
        entityManager.persistAndFlush(category);

        // WHEN
        Optional<Category> opt = categoryRepository.findById(category.getId());

        // THEN
        assertThat(opt).isPresent();
        assertThat(opt.get().getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should return empty Optional when ID does not exist")
    void shouldReturnEmpty_whenIdDoesNotExist() {
        // GIVEN
        Long nonExistentId = 999L;

        // WHEN
        Optional<Category> opt = categoryRepository.findById(nonExistentId);

        // THEN
        assertThat(opt).isEmpty();
    }

    // --- EXISTS BY NAME ---

    @Test
    @DisplayName("Should return true when name exists in database")
    void shouldReturnTrue_whenNameExists() {
        // GIVEN
        Category category = createEntity();
        entityManager.persistAndFlush(category);

        // WHEN
        boolean exists = categoryRepository.existsByName("Electronics");

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when name does not exist in database")
    void shouldReturnFalse_whenNameDoesNotExist() {
        // WHEN
        boolean exists = categoryRepository.existsByName("Ghost Category");

        // THEN
        assertThat(exists).isFalse();
    }

    // --- FIND ALL (PAGINATION) ---

    @Test
    @DisplayName("Should return paginated categories")
    void shouldReturnPaginatedCategories() {
        // GIVEN
        Category cat1 = createEntity();
        
        Category cat2 = new Category("Books", "Books category");
        
        Category cat3 = new Category("Toys", "Toys category");

        entityManager.persist(cat1);
        entityManager.persist(cat2);
        entityManager.persist(cat3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());

        // WHEN
        Page<Category> result = categoryRepository.findAll(pageable);

        // THEN
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Category::getName).containsExactlyInAnyOrder("Books", "Electronics");
    }

    // --- UPDATE ---

    @Test
    @DisplayName("Should update category fields and refresh updatedAt timestamp")
    void shouldUpdateCategoryFields() {
        // GIVEN
        Category category = createEntity();
        category = entityManager.persistAndFlush(category);
        LocalDateTime initialUpdateAt = category.getUpdatedAt();

        try {
            Thread.sleep(1); // Wait to ensure the timestamp changes
        } catch (InterruptedException e) {
        }

        // WHEN
        category.setName("Home Appliances");
        category.setDescription("New description");
        Category updatedCategory = categoryRepository.saveAndFlush(category);

        // THEN
        assertThat(updatedCategory.getName()).isEqualTo("Home Appliances");
        assertThat(updatedCategory.getDescription()).isEqualTo("New description");
        assertThat(updatedCategory.getUpdatedAt()).isAfter(initialUpdateAt); // Verifica @PreUpdate
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when updating name to duplicate")
    void shouldThrowException_whenUpdatingNameToDuplicate() {
        // GIVEN
        entityManager.persistAndFlush(createEntity());
        Category cat2 = entityManager.persistAndFlush(new Category("Books", "Books category"));

        // WHEN: Provo a cambiare il nome della cat2 con quella della cat1
        cat2.setName("Electronics");

        // THEN
        assertThatThrownBy(() -> categoryRepository.saveAndFlush(cat2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- DELETE ---

    @Test
    @DisplayName("Should delete category and confirm removal from database")
    void shouldDeleteCategory_andConfirmRemoval() {
        // GIVEN
        Category category = createEntity();
        category = entityManager.persistAndFlush(category);

        // WHEN
        categoryRepository.delete(category);
        entityManager.flush();

        // THEN
        Category deleted = entityManager.find(Category.class, category.getId());
        assertThat(deleted).isNull();
    }
}
