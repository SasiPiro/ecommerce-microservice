package com.ecommerce.user.service.impl;

import com.ecommerce.user.dto.LoginRequestDTO;
import com.ecommerce.user.dto.LoginResponseDTO;
import com.ecommerce.user.dto.UserRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.AuthService;
import com.ecommerce.user.utils.JwtUtil;
import com.ecommerce.user.utils.SecurityUtil;
import com.ecommerce.user.constant.UserPermissionConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    /**
     * Authenticate user using Spring Security and generate JWT token
     */
    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for: {}", request.usernameOrEmail());

        // Delegate authentication to Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usernameOrEmail(),
                        request.password()
                )
        );

        // Authentication successful, get email from principal
        String email = authentication.getName();

        log.debug("Authentication successful for: {}", email);

        // Fetch user with roles and permissions for response DTO
        User user = userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseGet(() -> userRepository.findByUsernameWithRolesAndPermissions(email)
                        .orElseThrow(() -> UserNotFoundException.forEmail(email))
                );

        // Generate JWT token
        String token = jwtUtil.generateToken(user);
        Long expiresIn = jwtUtil.getExpirationTime();

        // Map user to DTO
        UserResponseDTO userResponse = userMapper.toResponseDTO(user);

        log.info("Login successful for user: {} (ID: {})", user.getUsername(), user.getId());

        return new LoginResponseDTO(token, expiresIn, userResponse);
    }

    /**
     * Register new user with default CUSTOMER role
     */
    @Override
    public UserResponseDTO register(UserRequestDTO request) {
        log.info("Registration attempt for username: {}, email: {}",
                request.username(), request.email());

        if ( !SecurityUtil.hasPermission(UserPermissionConstant.USER_WRITE)) {
            throw new BadCredentialsException("Attempt to create user without permissions");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed: Username already exists: {}", request.username());
            throw UserAlreadyExistsException.forUsername();
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: Email already exists: {}", request.email());
            throw UserAlreadyExistsException.forEmail();
        }

        // Create new user
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());

        // Assign default USER role
        Role customerRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new UserNotFoundException("Default USER role not found. Please run data initialization."));

        user.addRole(customerRole);

        // Save user
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        return userMapper.toResponseDTO(savedUser);
    }
}
