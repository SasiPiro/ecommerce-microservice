package com.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank(message = "Username obbligatorio")
        @Size(min = 3, max = 50)
        String username,

        @NotBlank(message = "Email obbligatoria")
        @Email(message = "Formato email non valido")
        String email,

        @NotBlank(message = "Password obbligatoria")
        @Size(min = 6, max = 100)
        String password,

        @Size(min = 2, max = 50)
        String firstName,

        @Size(min = 2, max = 50)
        String lastName,

        String phone
) {}
