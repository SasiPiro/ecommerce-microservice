package com.ecommerce.user.util;

import com.ecommerce.user.dto.UserPutRequestDTO;
import com.ecommerce.user.dto.UserPutResponseDTO;
import com.ecommerce.user.dto.UserRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public UserResponseDTO generateDTOFromUser(User user){
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getUserRole(),
                user.getCreatedAt()
        );
    }

    public User generateUserFromDTO(UserRequestDTO userRequestDTO) {
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

    public UserPutResponseDTO generatePutResponseFromUser(User user) {
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
                user.getUpdatedAt()
        );
    }

    public User updateUserFromPutDTO(UserPutRequestDTO dto , User user) {
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhone(dto.phone());
        user.setActive(dto.active());
        user.setUserRole(dto.userRole());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
