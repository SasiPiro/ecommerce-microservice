package com.ecommerce.user.service;

import com.ecommerce.user.dto.LoginRequestDTO;
import com.ecommerce.user.dto.LoginResponseDTO;
import com.ecommerce.user.dto.UserRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;

public interface AuthService {

    public LoginResponseDTO login(LoginRequestDTO request);
    public UserResponseDTO register(UserRequestDTO request);
}
