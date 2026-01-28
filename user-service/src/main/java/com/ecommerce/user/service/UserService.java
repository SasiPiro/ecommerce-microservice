package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;

import java.util.List;

public interface UserService {

     UserResponseDTO createUser(UserRequestDTO userRequestDTO);
     List<UserResponseDTO> getAllUsers();
     UserResponseDTO findById(Long id);
     UserResponseDTO findByUsername(String username);
     UserResponseDTO findByEmail(String email);
     void deleteUser(Long id);
     UserResponseDTO patchUser(Long id, UserPatchRequestDTO userPatchRequestDTO);
     UserPutResponseDTO putUser(Long id , UserPutRequestDTO userPutRequestDTO);

}
