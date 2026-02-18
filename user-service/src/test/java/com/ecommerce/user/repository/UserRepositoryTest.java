package com.ecommerce.user.repository;

import com.ecommerce.user.model.User;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- FACTORY ---
    private User createEntity() {
        return new User(null, "mario88", "mario@test.it", "pass", "Mario", "Rossi", "123", User.UserRole.CUSTOMER);
    }

    // --- TEST CREATE ---

    @Test
    @DisplayName("Should persist user with generated ID and audit fields")
    void shouldPersistUser_withGeneratedIdAndAuditFields() {
        User user = createEntity();

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("mario88");
        assertThat(savedUser.isActive()).isTrue(); // Default del costruttore
        assertThat(savedUser.getCreatedAt()).isNotNull(); // Gestito da @PrePersist
        assertThat(savedUser.getUpdatedAt()).isNotNull(); // Gestito da costruttore
        assertThat(savedUser.getUserRole()).isEqualTo(User.UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when username is duplicate")
    void shouldThrowException_whenUsernameIsDuplicate() {
        // GIVEN: existing user
        entityManager.persistAndFlush(createEntity());

        // same username of the first user
        User user2 = new User(null, "mario88", "email2@test.it", "pass", "C", "D", "2", User.UserRole.CUSTOMER);

        // WHEN AND THEN
        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- RICERCA PER ID ---

    @Test
    @DisplayName("Should return user when searching by existing ID")
    void shouldReturnUser_whenIdExists() {
        // GIVEN
        User user = createEntity();
        entityManager.persistAndFlush(user);

        // WHEN
        Optional<User> opt = userRepository.findById(user.getId());

        // THEN
        assertThat(opt).isPresent();
        assertThat(opt.get().getUsername()).isEqualTo("mario88");
        assertThat(opt.get().getUserRole()).isEqualTo(User.UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("Should return empty Optional when ID does not exist")
    void shouldReturnEmpty_whenIdDoesNotExist() {
        // GIVEN
        Long idInesistente = 999L;

        // WHEN
        Optional<User> opt = userRepository.findById(idInesistente);

        // THEN
        assertThat(opt).isEmpty();
    }

    // --- RICERCA PER USERNAME ---

    @Test
    @DisplayName("Should return user when searching by existing username")
    void shouldReturnUser_whenUsernameExists() {
        // GIVEN
        User user = createEntity();
        entityManager.persistAndFlush(user);

        // WHEN
        Optional<User> opt = userRepository.findByUsername("mario88");

        // THEN
        assertThat(opt).isPresent();
        assertThat(opt.get().getEmail()).isEqualTo("mario@test.it");
    }

    @Test
    @DisplayName("Should return empty Optional when username does not exist")
    void shouldReturnEmpty_whenUsernameDoesNotExist() {
        // WHEN
        Optional<User> opt = userRepository.findByUsername("not_existing");

        // THEN
        assertThat(opt).isNotPresent();
    }

    // --- RICERCA PER EMAIL ---

    @Test
    @DisplayName("Should return user when searching by existing email")
    void shouldReturnUser_whenEmailExists() {
        // GIVEN
        User user = createEntity();
        entityManager.persistAndFlush(user);

        // WHEN
        Optional<User> opt = userRepository.findByEmail("mario@test.it");

        // THEN
        assertThat(opt).isPresent();
        assertThat(opt.get().getUsername()).isEqualTo("mario88");
    }

    @Test
    @DisplayName("Should return empty Optional when email does not exist")
    void shouldReturnEmpty_whenEmailDoesNotExist() {
        // WHEN
        Optional<User> opt = userRepository.findByEmail("null@email.com");

        // THEN
        assertThat(opt).isEmpty();
    }

    // --- EXISTS BY USERNAME ---

    @Test
    @DisplayName("Should return true when username exists in database")
    void shouldReturnTrue_whenUsernameExists() {
        // GIVEN
        User user = createEntity();
        entityManager.persistAndFlush(user);

        // WHEN
        boolean exists = userRepository.existsByUsername("mario88");

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist in database")
    void shouldReturnFalse_whenUsernameDoesNotExist() {
        // WHEN
        boolean exists = userRepository.existsByUsername("ghost_user");

        // THEN
        assertThat(exists).isFalse();
    }

    // --- EXISTS BY EMAIL ---

    @Test
    @DisplayName("Should return true when email exists in database")
    void shouldReturnTrue_whenEmailExists() {
        // GIVEN
        User user = createEntity();
        entityManager.persistAndFlush(user);

        // WHEN
        boolean exists = userRepository.existsByEmail("mario@test.it");

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist in database")
    void shouldReturnFalse_whenEmailDoesNotExist() {
        // WHEN
        boolean exists = userRepository.existsByEmail("not@exists.it");

        // THEN
        assertThat(exists).isFalse();
    }

    // --- FIND ALL ---

    @Test
    @DisplayName("Should return paginated and sorted users")
    void shouldReturnPaginatedAndSortedUsers() {
        // GIVEN
        User user1 = createEntity();

        User user2 = createEntity();
        user2.setUsername("u2");
        user2.setEmail("mail@test.it");

        User user3 = createEntity();
        user3.setUsername("u3");
        user3.setEmail("mail3@test.it");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        // Chiediamo la pagina 0 con dimensione 2, ordinata per username
        Pageable pageable = PageRequest.of(0, 2, Sort.by("username").ascending());

        // 2. WHEN
        Page<User> result = userRepository.findAll(pageable);

        // THEN
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).extracting(User::getUsername).containsExactlyInAnyOrder("mario88", "u2");
    }

    @Test
    @DisplayName("Should return empty page when database is empty")
    void shouldReturnEmptyPage_whenDatabaseIsEmpty() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);

        // WHEN
        Page<User> result = userRepository.findAll(pageable);

        // THEN
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty content when requested page index is out of bounds")
    void shouldReturnEmptyContent_whenPageIndexIsOutOfBounds() {
        // 1. GIVEN: Salviamo 2 utenti nel DB
        entityManager.persist(new User(null, "u1", "u1@e.it", "p", "A", "B", "1", User.UserRole.CUSTOMER));
        entityManager.persist(createEntity());
        entityManager.flush();
        // Chiediamo la pagina 5 (che non esiste, dato che con size 10 avremmo solo la
        // pagina 0)
        Pageable pageable = PageRequest.of(5, 10);

        // 2. WHEN
        Page<User> result = userRepository.findAll(pageable);

        // 3. THEN
        assertThat(result.getContent()).isEmpty(); // Nessun dato per questa pagina
        assertThat(result.getTotalElements()).isEqualTo(2); // Ma il totale complessivo rimane 2
        assertThat(result.getTotalPages()).isEqualTo(1); // Le pagine totali rimangono 1
        assertThat(result.getNumber()).isEqualTo(5); // La pagina richiesta era la 5
    }

    // --- UPDATE ---

    @Test
    @DisplayName("Should update user fields and refresh updatedAt timestamp")
    void shouldUpdateUserFields_andRefreshUpdatedAtTimestamp() {
        // GIVEN
        User user = createEntity();
        user = entityManager.persistAndFlush(user);
        LocalDateTime initialUpdateAt = user.getUpdatedAt();

        // Aspettiamo un millisecondo per garantire che il tempo cambi per il test del
        // timestamp
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }

        // WHEN
        user.setFirstName("Luigi");
        User updatedUser = userRepository.saveAndFlush(user);

        // THEN
        assertThat(updatedUser.getFirstName()).isEqualTo("Luigi");
        assertThat(updatedUser.getUpdatedAt()).isAfter(initialUpdateAt); // Verifica @PreUpdate
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when updating email to an existing one")
    void shouldThrowException_whenUpdatingEmailToDuplicate() {
        // GIVEN
        entityManager.persistAndFlush(createEntity());
        User u2 = entityManager
                .persistAndFlush(new User(null, "user2", "email2@test.it", "p", "A", "B", "1", User.UserRole.CUSTOMER));

        // WHEN: Provo a cambiare l'email di u2 con quella di u1
        u2.setEmail("mario@test.it");

        // THEN
        assertThatThrownBy(() -> userRepository.saveAndFlush(u2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- DELETE ---

    @Test
    @DisplayName("Should delete user and confirm removal from database")
    void shouldDeleteUser_andConfirmRemovalFromDatabase() {
        // GIVEN
        User user = createEntity();
        user = entityManager.persistAndFlush(user);

        // WHEN
        userRepository.delete(user);
        entityManager.flush();

        // THEN
        User deleted = entityManager.find(User.class, user.getId());
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("Should not throw exception when deleting a non-existent ID")
    void shouldNotThrowException_whenDeletingNonExistentId() {
        // GIVEN: un ID non presente nel DB
        Long nonExistentId = 999L;

        // WHEN AND THEN

        // deleteById su un ID inesistente non fa nulla nelle nuove versioni di spring
        assertThatCode(() -> userRepository.deleteById(nonExistentId))
                .doesNotThrowAnyException();

    }
}
