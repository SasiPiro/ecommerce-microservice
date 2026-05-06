package com.ecommerce.user.repository;

import com.ecommerce.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    // CRITICO per evitare N+1 queries
    /**
     * Fetch user con roles e permissions in una singola query
     * per autenticazione (UserDetailsService)
     */
    @Query("""
        SELECT DISTINCT u 
        FROM User u 
        LEFT JOIN FETCH u.roles r 
        LEFT JOIN FETCH r.permissions 
        WHERE u.email = :email
        """)
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    /**
     * Fetch user con roles e permissions usando username
     * Alternativa se login è con username invece di email
     */
    @Query("""
        SELECT DISTINCT u 
        FROM User u 
        LEFT JOIN FETCH u.roles r 
        LEFT JOIN FETCH r.permissions 
        WHERE u.username = :username
        """)
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);
}
