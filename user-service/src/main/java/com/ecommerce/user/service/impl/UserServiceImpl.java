package com.ecommerce.user.service.impl;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.utils.UserMapper;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO){
        if(userRepository.existsByUsername(userRequestDTO.username())){
            throw new UserAlreadyExistsException("Username already in use");
        }
        if (userRepository.existsByEmail(userRequestDTO.email())) {
            throw new UserAlreadyExistsException("Email already associated");
        }
        User newUser = userMapper.generateUserFromDTO(userRequestDTO);
        userRepository.save(newUser);
        return userMapper.generateDTOFromUser(newUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(userMapper::generateDTOFromUser)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id){
            return userRepository.findById(id)
                    .map(userMapper::generateDTOFromUser)
                    .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
        public UserResponseDTO findByUsername(String username){
        return userRepository.findByUsername(username)
                .map(userMapper::generateDTOFromUser)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findByEmail(String email){
        return userRepository.findByEmail(email)
                .map(userMapper::generateDTOFromUser)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    @Override
    public void deleteUser(Long id){

        if(!userRepository.existsById(id)) {
            throw new UserNotFoundException("Cannot delete user with ID : " + id + " - ID not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponseDTO patchUser(Long id, UserPatchRequestDTO userPatchRequestDTO) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        if (StringUtils.hasText(userPatchRequestDTO.firstName())) {
            user.setFirstName(userPatchRequestDTO.firstName());
        }

        if (StringUtils.hasText(userPatchRequestDTO.lastName())) {
            user.setLastName(userPatchRequestDTO.lastName());
        }

        if (StringUtils.hasText(userPatchRequestDTO.phone())) {
            user.setPhone(userPatchRequestDTO.phone());
        }

        if (StringUtils.hasText(userPatchRequestDTO.email())) {
            if (!userPatchRequestDTO.email().equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(userPatchRequestDTO.email())) {
                    throw new UserAlreadyExistsException("Email already associated");
                }
                user.setEmail(userPatchRequestDTO.email());
            }
        }
        return userMapper.generateDTOFromUser(userRepository.save(user));
    }

    @Override
    public UserPutResponseDTO putUser(Long id, UserPutRequestDTO userPutRequestDTO) {
        if(userRepository.existsByUsername(userPutRequestDTO.username())){
            throw new UserAlreadyExistsException("Username already in use");
        }
        if (userRepository.existsByEmail(userPutRequestDTO.email())) {
            throw new UserAlreadyExistsException("Email already associated");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        User updatedUser = userMapper.updateUserFromPutDTO(userPutRequestDTO , user);
        userRepository.save(updatedUser);
        return userMapper.generatePutResponseFromUser(updatedUser);
    }
}
