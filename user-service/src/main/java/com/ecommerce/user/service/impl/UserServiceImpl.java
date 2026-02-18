package com.ecommerce.user.service.impl;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.util.UserMapper;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.ecommerce.user.constant.LogCode.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        log.info("Creating user - username: '{}', email: '{}'", userRequestDTO.username(), userRequestDTO.email());

        if (userRepository.existsByUsername(userRequestDTO.username())) {
            log.warn("[{}] Registration rejected - username '{}' already exists",
                    USERNAME_ALREADY_EXISTS, userRequestDTO.username());
            throw UserAlreadyExistsException.forUsername();
        }
        if (userRepository.existsByEmail(userRequestDTO.email())) {
            log.warn("[{}] Registration rejected - email '{}' already exists",
                    EMAIL_ALREADY_EXISTS, userRequestDTO.email());
            throw UserAlreadyExistsException.forEmail();
        }

        User newUser = userMapper.generateUserFromDTO(userRequestDTO);
        userRepository.save(newUser);

        log.info("User created successfully - id: {}, username: '{}'", newUser.getId(), newUser.getUsername());
        return userMapper.generateDTOFromUser(newUser);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching users - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<UserResponseDTO> result = userRepository.findAll(pageable)
                .map(userMapper::generateDTOFromUser);

        log.debug("Returned {} user(s) out of {} total", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        log.debug("Looking up user by id: {}", id);
        return userRepository.findById(id)
                .map(userMapper::generateDTOFromUser)
                .orElseThrow(() -> {
                    log.warn("[{}] User not found - id: {}", USER_NOT_FOUND, id);
                    return UserNotFoundException.forId();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findByUsername(String username) {
        log.debug("Looking up user by username: '{}'", username);
        return userRepository.findByUsername(username)
                .map(userMapper::generateDTOFromUser)
                .orElseThrow(() -> {
                    log.warn("[{}] User not found - username: '{}'", USER_NOT_FOUND, username);
                    return UserNotFoundException.forUsername(username);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findByEmail(String email) {
        log.debug("Looking up user by email: '{}'", email);
        return userRepository.findByEmail(email)
                .map(userMapper::generateDTOFromUser)
                .orElseThrow(() -> {
                    log.warn("[{}] User not found - email: '{}'", USER_NOT_FOUND, email);
                    return UserNotFoundException.forEmail(email);
                });
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user - id: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("[{}] Delete rejected - user not found - id: {}", USER_NOT_FOUND, id);
            throw UserNotFoundException.forId();
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully - id: {}", id);
    }

    // -------------------------------------------------------------------------
    // UPDATE - partial (PATCH)
    // -------------------------------------------------------------------------

    @Override
    public UserResponseDTO patchUser(Long id, UserPatchRequestDTO userPatchRequestDTO) {
        log.info("Patching user - id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Patch rejected - user not found - id: {}", USER_NOT_FOUND, id);
                    return UserNotFoundException.forId();
                });

        if (StringUtils.hasText(userPatchRequestDTO.firstName())) {
            log.debug("Patching firstName: '{}' -> '{}'", user.getFirstName(), userPatchRequestDTO.firstName());
            user.setFirstName(userPatchRequestDTO.firstName());
        }
        if (StringUtils.hasText(userPatchRequestDTO.lastName())) {
            log.debug("Patching lastName: '{}' -> '{}'", user.getLastName(), userPatchRequestDTO.lastName());
            user.setLastName(userPatchRequestDTO.lastName());
        }
        if (StringUtils.hasText(userPatchRequestDTO.phone())) {
            log.debug("Patching phone: '{}' -> '{}'", user.getPhone(), userPatchRequestDTO.phone());
            user.setPhone(userPatchRequestDTO.phone());
        }

        // Email update: skip if unchanged (case-insensitive), reject if already taken
        if (StringUtils.hasText(userPatchRequestDTO.email())) {
            if (!userPatchRequestDTO.email().equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(userPatchRequestDTO.email())) {
                    log.warn("[{}] Patch rejected - email '{}' already taken by another user",
                            EMAIL_ALREADY_EXISTS, userPatchRequestDTO.email());
                    throw UserAlreadyExistsException.forEmail();
                }
                log.debug("Patching email: '{}' -> '{}'", user.getEmail(), userPatchRequestDTO.email());
                user.setEmail(userPatchRequestDTO.email());
            }
        }

        UserResponseDTO response = userMapper.generateDTOFromUser(userRepository.save(user));
        log.info("User patched successfully - id: {}", id);
        return response;
    }

    // -------------------------------------------------------------------------
    // UPDATE - full replacement (PUT)
    // -------------------------------------------------------------------------

    @Override
    public UserPutResponseDTO putUser(Long id, UserPutRequestDTO userPutRequestDTO) {
        log.info("Full update (PUT) - id: {}, new username: '{}', new email: '{}'",
                id, userPutRequestDTO.username(), userPutRequestDTO.email());

        // Uniqueness checks are performed before loading the entity to fail fast
        if (userRepository.existsByUsername(userPutRequestDTO.username())) {
            log.warn("[{}] PUT rejected - username '{}' already taken by another user",
                    USERNAME_ALREADY_EXISTS, userPutRequestDTO.username());
            throw UserAlreadyExistsException.forUsername();
        }
        if (userRepository.existsByEmail(userPutRequestDTO.email())) {
            log.warn("[{}] PUT rejected - email '{}' already taken by another user",
                    EMAIL_ALREADY_EXISTS, userPutRequestDTO.email());
            throw UserAlreadyExistsException.forEmail();
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] PUT rejected - user not found - id: {}", USER_NOT_FOUND, id);
                    return UserNotFoundException.forId();
                });

        User updatedUser = userMapper.updateUserFromPutDTO(userPutRequestDTO, user);
        userRepository.save(updatedUser);

        log.info("User updated successfully - id: {}", id);
        return userMapper.generatePutResponseFromUser(updatedUser);
    }
}
