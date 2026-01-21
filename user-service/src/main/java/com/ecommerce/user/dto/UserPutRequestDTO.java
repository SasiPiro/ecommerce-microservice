package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserPutRequestDTO(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(max = 50) @Email(message = "Formato email non valido")  String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        String firstName,
        String lastName,
        String phone,
        boolean active,
        @NotNull User.UserRole userRole) {}
