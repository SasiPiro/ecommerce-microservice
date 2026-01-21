package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;

import java.time.LocalDateTime;

public record UserPutResponseDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        boolean active,
        User.UserRole userRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
