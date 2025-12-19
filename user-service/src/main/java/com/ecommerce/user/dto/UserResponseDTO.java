package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;
import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        User.UserRole userRole,
        LocalDateTime createdAt
) {}
