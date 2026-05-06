package com.ecommerce.user.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.ecommerce.user.model.Permission;
import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class JwtUserService implements UserDetailsService {

    private final UserRepository userRepository;

    public JwtUserService(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        // Try email first, then username
        User user = userRepository.findByEmailWithRolesAndPermissions(usernameOrEmail)
                .orElseGet(() -> userRepository.findByUsernameWithRolesAndPermissions(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException( "User not found: " + usernameOrEmail))
                );

        return buildUserDetails(user);
    }

    /**
     * Converte User entity (domain) in UserDetails (Spring Security)
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isActive())
                .authorities(getAuthorities(user))
                .build();
    }

    /**
     * Estrae tutte le authorities (permissions + roles) dall'utente
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Aggiungi tutte le permissions
        user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .forEach(permissionName ->
                        authorities.add(new SimpleGrantedAuthority(permissionName))
                );

        // 2. Aggiungi i ruoli con prefisso ROLE_ (per @PreAuthorize("hasRole('ADMIN')"))
        user.getRoles().stream()
                .map(Role::getName)
                .forEach(roleName ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName))
                );

        return authorities;
    }

}
