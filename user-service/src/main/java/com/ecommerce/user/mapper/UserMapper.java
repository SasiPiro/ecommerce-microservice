package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.UserPutRequestDTO;
import com.ecommerce.user.dto.UserPutResponseDTO;
import com.ecommerce.user.dto.UserRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.model.Permission;
import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) return null;
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                extractRoleNames(user.getRoles()),
                extractPermissionNames(user.getRoles()),
                user.getCreatedAt());
    }

    public User toEntity(UserRequestDTO userRequestDTO) {
        if (userRequestDTO == null) return null;
        User newUser = new User();
        newUser.setUsername(userRequestDTO.username());
        newUser.setEmail(userRequestDTO.email());
        newUser.setFirstName(userRequestDTO.firstName());
        newUser.setLastName(userRequestDTO.lastName());
        newUser.setPhone(userRequestDTO.phone());
        // Default role al momento newHashSet
        newUser.setPassword(userRequestDTO.password());
        return newUser;
    }

    public UserPutResponseDTO toPutResponseDTO(User user) {
        if (user == null) return null;
        return new UserPutResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.isActive(),
                extractRoleNames(user.getRoles()),
                extractPermissionNames(user.getRoles()),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public User updateEntityFromPutDTO(UserPutRequestDTO dto, User user) {
        if (dto == null) return user;
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhone(dto.phone());
        user.setActive(dto.active());
        return user;
    }

    /**
     * Estrae i nomi dei ruoli da un Set di entità Role.
     */
    public static Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Estrae i nomi delle permission (univoche) partendo da un Set di ruoli.
     */
    public static Set<String> extractPermissionNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }
        return roles.stream()
                // Assicura che le permission del ruolo non siano null
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
