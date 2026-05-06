package com.ecommerce.user.controller;

import com.ecommerce.user.dto.LoginRequestDTO;
import com.ecommerce.user.dto.LoginResponseDTO;
import com.ecommerce.user.dto.UserRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.service.AuthService;
import com.ecommerce.user.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController (AuthServiceImpl authService){
        this.authService = authService;
    }

    @PostMapping("/loginApi")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) throws Exception{
        return ResponseEntity.ok(authService.login(request));
    }
    
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('user.write')")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO request) throws Exception{
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}
