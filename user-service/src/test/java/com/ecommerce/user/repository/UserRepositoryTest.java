package com.ecommerce.user.repository;

import com.ecommerce.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = RepositoryConfig.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- TEST CREATE ---

    @Test
    public void testCreateOk(){
        User user = new User(null , "nick", "mail@mail.com", "password", "mario", "rossi", "3331234567", User.UserRole.CUSTOMER);

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("nick");
        assertThat(savedUser.isActive()).isTrue(); // Default del costruttore
        assertThat(savedUser.getCreatedAt()).isNotNull(); // Gestito da @PrePersist
        assertThat(savedUser.getUpdatedAt()).isNotNull(); // Gestito da costruttore
        assertThat(savedUser.getUserRole()).isEqualTo(User.UserRole.CUSTOMER);
    }

    @Test
    public void testCreateKo_DuplicateUsername() {
        // GIVEN: Un utente già esistente
        User user1 = new User(null, "duplicato", "email1@test.it", "pass", "A", "B", "1", User.UserRole.CUSTOMER);
        entityManager.persistAndFlush(user1);

        // Un secondo utente con lo stesso username
        User user2 = new User(null, "duplicato", "email2@test.it", "pass", "C", "D", "2", User.UserRole.CUSTOMER);

        // WHEN AND THEN
        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- RICERCA PER ID ---

    @Test
    public void testFindByIdOk() {
        // GIVEN
        User user = new User(null, "user1", "user1@email.com", "pass", "N", "C", "1", User.UserRole.ADMIN);
        entityManager.persistAndFlush(user);

        // WHEN
        Optional<User> opt = userRepository.findById(user.getId());

        // THEN
        assertThat(opt).isPresent();
        assertThat(opt.get().getUsername()).isEqualTo("user1");
        assertThat(opt.get().getUserRole()).isEqualTo(User.UserRole.ADMIN);
    }

    @Test
    public void testFindByIdKo() {
        // GIVEN
        Long idInesistente = 999L;

        // WHEN
        Optional<User> opt = userRepository.findById(idInesistente);

        // THEN
        assertThat(opt).isEmpty();
    }

    // --- RICERCA PER USERNAME ---

    @Test
    public void testFindByUsernameOk() {
        // GIVEN
        User user = new User(null, "test_user", "test@email.com", "pass", "N", "C", "1", User.UserRole.SELLER);
        entityManager.persistAndFlush(user);

        // WHEN
        Optional<User> opt = userRepository.findByUsername("test_user");

        // THEN
        assertThat(opt).isPresent();
        assertThat(opt.get().getEmail()).isEqualTo("test@email.com");
    }

    @Test
    public void testFindByUsernameKo() {
        // WHEN
        Optional<User> opt = userRepository.findByUsername("non_esisto");

        // THEN
        assertThat(opt).isNotPresent();
    }

    // --- RICERCA PER EMAIL ---

    @Test
    public void testFindByEmailOk() {
        // GIVEN
        User user = new User(null, "email_user", "find@email.com", "pass", "N", "C", "1", User.UserRole.CUSTOMER);
        entityManager.persistAndFlush(user);

        // WHEN
        Optional<User> opt = userRepository.findByEmail("find@email.com");

        // THEN
        assertThat(opt).isPresent();
        assertThat(opt.get().getUsername()).isEqualTo("email_user");
    }

    @Test
    public void testFindByEmailKo() {
        // WHEN
        Optional<User> opt = userRepository.findByEmail("null@email.com");

        // THEN
        assertThat(opt).isEmpty();
    }

    // --- EXISTS BY USERNAME ---

    @Test
    public void testExistsByUsernameOk() {
        // GIVEN
        entityManager.persistAndFlush(new User(null, "boss", "boss@email.com", "p", "A", "B", "1", User.UserRole.ADMIN));

        // WHEN
        boolean exists = userRepository.existsByUsername("boss");

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByUsernameKo() {
        // WHEN
        boolean exists = userRepository.existsByUsername("ghost_user");

        // THEN
        assertThat(exists).isFalse();
    }

    // --- EXISTS BY EMAIL ---

    @Test
    public void testExistsByEmailOk() {
        // GIVEN
        entityManager.persistAndFlush(new User(null, "mail_test", "exists@test.it", "p", "A", "B", "1", User.UserRole.CUSTOMER));

        // WHEN
        boolean exists = userRepository.existsByEmail("exists@test.it");

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByEmailKo() {
        // WHEN
        boolean exists = userRepository.existsByEmail("not@exists.it");

        // THEN
        assertThat(exists).isFalse();
    }

    // --- FIND ALL ---

    @Test
    public void testFindAllOk() {
        // GIVEN
        entityManager.persist(new User(null, "u1", "u1@e.it", "p", "A", "B", "1", User.UserRole.CUSTOMER));
        entityManager.persist(new User(null, "u2", "u2@e.it", "p", "A", "B", "1" , User.UserRole.SELLER));
        entityManager.flush();

        // WHEN
        List<User> users = userRepository.findAll();

        // THEN
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername).containsExactlyInAnyOrder("u1", "u2");
    }

    @Test
    public void testFindAllKo() {
        // WHEN
        List<User> users = userRepository.findAll();

        // THEN
        assertThat(users).isEmpty();
    }

    // --- UPDATE ---

    @Test
    public void testUpdateOk() {
        // GIVEN
        User user = new User(null, "original", "original@email.com", "pass", "Mario", "Rossi", "1", User.UserRole.CUSTOMER);
        user = entityManager.persistAndFlush(user);
        LocalDateTime initialUpdateAt = user.getUpdatedAt();

        // Aspettiamo un millisecondo per garantire che il tempo cambi per il test del timestamp
        try { Thread.sleep(1); } catch (InterruptedException e) {}

        // WHEN
        user.setFirstName("Luigi");
        User updatedUser = userRepository.saveAndFlush(user);

        // THEN
        assertThat(updatedUser.getFirstName()).isEqualTo("Luigi");
        assertThat(updatedUser.getUpdatedAt()).isAfter(initialUpdateAt); // Verifica @PreUpdate
    }

    @Test
    public void testUpdateKo_DuplicateEmail() {
        // GIVEN
        User u1 = entityManager.persistAndFlush(new User(null, "user1", "email1@test.it", "p", "A", "B", "1", User.UserRole.CUSTOMER));
        User u2 = entityManager.persistAndFlush(new User(null, "user2", "email2@test.it", "p", "A", "B", "1", User.UserRole.CUSTOMER));

        // WHEN: Provo a cambiare l'email di u2 con quella di u1
        u2.setEmail("email1@test.it");

        // THEN
        assertThatThrownBy(() -> userRepository.saveAndFlush(u2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- DELETE ---

    @Test
    public void testDeleteOk() {
        // GIVEN
        User user = new User(null, "delete_me", "del@test.it", "p", "A", "B", "1", User.UserRole.CUSTOMER);
        user = entityManager.persistAndFlush(user);

        // WHEN
        userRepository.delete(user);
        entityManager.flush();

        // THEN
        User deleted = entityManager.find(User.class, user.getId());
        assertThat(deleted).isNull();
    }

    @Test
    public void testDeleteKo() {
        // GIVEN: un ID non presente nel DB
        Long nonExistentId = 999L;

        // WHEN AND THEN

        // deleteById su un ID inesistente non fa nulla nelle nuove versioni di spring
        assertThatCode(() -> userRepository.deleteById(nonExistentId))
                .doesNotThrowAnyException();

    }
}
