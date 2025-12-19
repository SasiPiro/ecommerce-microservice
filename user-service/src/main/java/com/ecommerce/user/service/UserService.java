package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserPatchRequestDTO;
import com.ecommerce.user.dto.UserRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.exception.OperationNotPermittedException;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO){
        if(userRepository.existsByUsername(userRequestDTO.username())){
            throw new UserAlreadyExistsException("Username already in use");
        }
        if (userRepository.existsByEmail(userRequestDTO.email())) {
            throw new UserAlreadyExistsException("Email already associated");
        }

        User savedUser = userRepository.save(generateUserFromDTO(userRequestDTO));

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getUserRole(),
                savedUser.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(this::generateDTOFromUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id){
            return userRepository.findById(id)
                    .map(this::generateDTOFromUser)
                    .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findByUsername(String username){
        return userRepository.findByUsername(username)
                .map(this::generateDTOFromUser)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findByEmail(String email){
        return userRepository.findByUsername(email)
                .map(this::generateDTOFromUser)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    public void deleteUser(Long id){

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Cannot delete user with ID : " + id + " - ID not found"));

        if (user.getUserRole() == User.UserRole.ADMIN) {
            throw new OperationNotPermittedException("Cannot delete an Admin");
        }
        userRepository.delete(user);
    }

    public UserResponseDTO patchUser(Long id, UserPatchRequestDTO userPatchRequestDTO) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        if (userPatchRequestDTO.firstName() != null && !userPatchRequestDTO.firstName().isBlank()) {
                user.setFirstName(userPatchRequestDTO.firstName());
        }

        if (userPatchRequestDTO.lastName() != null && !userPatchRequestDTO.lastName().isBlank()) {
            user.setLastName(userPatchRequestDTO.lastName());
        }

        if (userPatchRequestDTO.phone() != null && !userPatchRequestDTO.phone().isBlank()) {
            user.setPhone(userPatchRequestDTO.phone());
        }

        if (userPatchRequestDTO.email() != null && !userPatchRequestDTO.email().isBlank()) {
            if (!userPatchRequestDTO.email().equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(userPatchRequestDTO.email())) {
                    throw new UserAlreadyExistsException("Email already associated");
                }
                user.setEmail(userPatchRequestDTO.email());
            }
        }
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return generateDTOFromUser(user);
    }

    private UserResponseDTO generateDTOFromUser(User user){
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserRole(),
                user.getCreatedAt()
        );
    }

    private User generateUserFromDTO(UserRequestDTO userRequestDTO) {
        User newUser = new User();
        newUser.setUsername(userRequestDTO.username());
        newUser.setEmail(userRequestDTO.email());
        newUser.setFirstName(userRequestDTO.firstName());
        newUser.setLastName(userRequestDTO.lastName());
        newUser.setPhone(userRequestDTO.phone());
        newUser.setUserRole(User.UserRole.CUSTOMER); // Default role
        // TODO: Ricordati di hashare la password qui!
        newUser.setPassword(userRequestDTO.password());
        return newUser;
    }

}
