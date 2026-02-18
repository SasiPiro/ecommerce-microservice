package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing the lifecycle of {@link User} entities.
 * <p>
 * This contract defines operations for creating, retrieving, updating, and deleting users.
 * It enforces business rules such as email and username uniqueness.
 * </p>
 *
 * @author Salvatore Pirozzi
 * @version 1.0
 */
public interface UserService {

     /**
      * Registers a new user in the system.
      * <p>
      * This method enforces uniqueness for both username and email.
      * </p>
      *
      * @param userRequestDTO The data transfer object containing the new user's information.
      * @return The persisted user mapped to a response DTO.
      * @throws UserAlreadyExistsException if the username or email is already in use.
      */
     UserResponseDTO createUser(UserRequestDTO userRequestDTO);

     /**
      * Retrieves a paginated list of all users.
      * <p>
      * Designed for administrative lists. Returns a {@link Page} container
      * including metadata (total pages, total elements) for frontend pagination.
      * </p>
      *
      * @param pageable The pagination information (page number, size, and sorting).
      * @return A page of {@link UserResponseDTO}.
      */
     Page<UserResponseDTO> getAllUsers(Pageable pageable);

     /**
      * Retrieves a specific user by their unique identifier.
      *
      * @param id The unique ID of the user.
      * @return The requested user DTO.
      * @throws UserNotFoundException if no user is found with the provided ID.
      */
     UserResponseDTO findById(Long id);

     /**
      * Retrieves a specific user by their username.
      *
      * @param username The unique username to search for.
      * @return The requested user DTO.
      * @throws UserNotFoundException if no user is found with the provided username.
      */
     UserResponseDTO findByUsername(String username);

     /**
      * Retrieves a specific user by their email address.
      *
      * @param email The unique email to search for.
      * @return The requested user DTO.
      * @throws UserNotFoundException if no user is found with the provided email.
      */
     UserResponseDTO findByEmail(String email);

     /**
      * Deletes a user from the system by their ID.
      * <p>
      * This operation checks for existence before deletion to ensure the ID is valid.
      * </p>
      *
      * @param id The unique ID of the user to delete.
      * @throws UserNotFoundException if the user does not exist.
      */
     void deleteUser(Long id);

     /**
      * Applies a partial update to an existing user resource (PATCH).
      * <p>
      * Only the fields present (non-null and containing text) in the {@link UserPatchRequestDTO}
      * will be updated. If the email is being changed, uniqueness is verified.
      * </p>
      *
      * @param id The ID of the user to update.
      * @param userPatchRequestDTO The DTO containing the fields to update.
      * @return The updated user DTO.
      * @throws UserNotFoundException if the user is not found.
      * @throws UserAlreadyExistsException if the new email is already taken by another user.
      */
     UserResponseDTO patchUser(Long id, UserPatchRequestDTO userPatchRequestDTO);

     /**
      * Performs a full update of an existing user resource (PUT).
      * <p>
      * This method replaces the user's mutable data with the provided DTO.
      * It validates that the new data (username/email) does not conflict with existing records.
      * </p>
      *
      * @param id The ID of the user to update.
      * @param userPutRequestDTO The DTO containing the new state of the user.
      * @return A specialized response DTO containing the updated data.
      * @throws UserNotFoundException if the user is not found.
      * @throws UserAlreadyExistsException if the updated username or email is already in use.
      */
     UserPutResponseDTO putUser(Long id, UserPutRequestDTO userPutRequestDTO);
}
