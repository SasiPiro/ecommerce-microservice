package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.UserPutRequestDTO;
import com.ecommerce.user.dto.UserPutResponseDTO;
import com.ecommerce.user.dto.UserRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.model.User;
import org.springframework.stereotype.Component;

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
                user.getUserRole(),
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
        newUser.setUserRole(User.UserRole.CUSTOMER); // Default role
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
                user.getUserRole(),
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
        user.setUserRole(dto.userRole());
        return user;
    }
}
