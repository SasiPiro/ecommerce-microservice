package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.impl.UserServiceImpl;
import com.ecommerce.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    // --- CONSTANTS ---
    private static final Long VALID_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final String MSG_USERNAME_TAKEN = "Username already in use";
    private static final String MSG_EMAIL_TAKEN = "Email already associated";
    private static final String MSG_NOT_FOUND = "User not found";

    // --- FACTORIES (Maintainability) ---
    private User createEntity() {
        return new User(VALID_ID, "mario88", "mario@test.it", "pass", "Mario", "Rossi", "123", User.UserRole.CUSTOMER);
    }

    // Factory per POST
    private UserRequestDTO createRequest() {
        return new UserRequestDTO("mario88", "mario@test.it", "pass", "Mario", "Rossi", "123");
    }

    // Factory per GET/PATCH Response
    private UserResponseDTO createResponse() {
        return new UserResponseDTO(VALID_ID, "mario88", "mario@test.it", "Mario", "Rossi", "123",
                User.UserRole.CUSTOMER, null);
    }

    // Factory per PUT Request
    private UserPutRequestDTO createPutRequest() {
        return new UserPutRequestDTO("mario88", "mario@test.it", "newPass", "Mario", "Rossi", "123", true,
                User.UserRole.CUSTOMER);
    }

    // Factory per PUT Response
    private UserPutResponseDTO createPutResponse() {
        return new UserPutResponseDTO(VALID_ID, "mario88", "mario@test.it", "Mario", "Rossi", "123", true,
                User.UserRole.CUSTOMER, null, null);
    }

    // --- 1. CREATE (POST) ---

    @Test
    @DisplayName("Should create user successfully when username and email are available")
    void shouldCreateUserSuccessfully_whenCredentialsAreAvailable() {
        // GIVEN
        UserRequestDTO request = createRequest();
        User newUser = createEntity();
        UserResponseDTO expectedResponse = createResponse();

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.generateUserFromDTO(request)).thenReturn(newUser);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.generateDTOFromUser(newUser)).thenReturn(expectedResponse);
        // WHEN
        UserResponseDTO response = userService.createUser(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo(request.username());
        assertThat(response.email()).isEqualTo(request.email());

        verify(userRepository).existsByUsername(request.username());
        verify(userRepository).existsByEmail(request.email());
        verify(userMapper).generateUserFromDTO(request);
        verify(userRepository).save(newUser);
        verify(userMapper).generateDTOFromUser(newUser);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username is already taken")
    void shouldThrowException_whenUsernameAlreadyExists() {
        // GIVEN
        UserRequestDTO request = createRequest();

        // Mock: userRepository username exist
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage(MSG_USERNAME_TAKEN);

        // VERIFY
        verify(userRepository).existsByUsername(request.username());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userMapper, never()).generateUserFromDTO(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email is already associated")
    void shouldThrowException_whenEmailAlreadyExists() {
        // GIVEN
        UserRequestDTO request = createRequest();

        // Mock: Free Username, Email existing
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage(MSG_EMAIL_TAKEN);

        // Let's verify that, although the preliminary checks have partially passed,
        // the entity has not been created.
        verify(userRepository).existsByUsername(request.username());
        verify(userRepository).existsByEmail(request.email());
        verify(userMapper, never()).generateUserFromDTO(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return paginated list of all users")
    void shouldReturnPaginatedUsers_whenUsersExist() {

        // GIVEN
        User user1 = createEntity();
        User user2 = new User(2L, "gianni", "email2@test.it", "pass", "A", "B", "1", User.UserRole.CUSTOMER);
        UserResponseDTO responseDTO1 = createResponse();
        UserResponseDTO responseDTO2 = new UserResponseDTO(user2.getId(), user2.getUsername(), user2.getEmail(),
                user2.getFirstName(), user2.getLastName(), user2.getPhone(), user2.getUserRole(), null);

        // Definiamo il Pageable del test
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username").ascending());

        // Creiamo la Page di Entity che il Repository restituirebbe
        List<User> userEntities = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(userEntities, pageable, userEntities.size());

        // MOCKING
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.generateDTOFromUser(user1)).thenReturn(responseDTO1);
        when(userMapper.generateDTOFromUser(user2)).thenReturn(responseDTO2);

        // WHEN
        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        // THEN
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(UserResponseDTO::username)
                .containsExactlyInAnyOrder(responseDTO1.username(), responseDTO2.username());

        // VERIFY
        verify(userRepository).findAll(pageable);
        verify(userMapper, times(2)).generateDTOFromUser(any(User.class));
    }

    @Test
    @DisplayName("Should return empty page when no users exist in database")
    void shouldReturnEmptyPage_whenNoUsersExist() {
        // 1. GIVEN: Definiamo un Pageable di richiesta
        Pageable pageable = PageRequest.of(0, 10);

        // Creiamo una Page vuota usando Page.empty()
        when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty(pageable));

        // 2. WHEN
        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        // THEN
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(userRepository).findAll(pageable);
        verifyNoInteractions(userMapper); // nothing to map if the page is empty
    }

    @Test
    @DisplayName("Should return user when searching by valid ID")
    void shouldReturnUser_whenIdExists() {
        // GIVEN
        User user = createEntity();

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(user));
        when(userMapper.generateDTOFromUser(user)).thenReturn(createResponse());

        // WHEN
        UserResponseDTO response = userService.findById(VALID_ID);

        // THEN
        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.email()).isEqualTo(user.getEmail());
        verify(userRepository).findById(user.getId());
        verify(userMapper).generateDTOFromUser(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when ID does not exist")
    void shouldThrowException_whenIdNotFound() {
        // GIVEN

        when(userRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // WHEN
        assertThatThrownBy(() -> userService.findById(NON_EXISTENT_ID))
                .isInstanceOf(UserNotFoundException.class) // Check the exception type
                .hasMessageContaining(MSG_NOT_FOUND);// Check if the message is correct

        // Verify Mapper never used
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should return user when searching by existing username")
    void shouldReturnUser_whenUsernameExists() {
        // GIVEN
        User user = createEntity();
        UserResponseDTO response = createResponse();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userMapper.generateDTOFromUser(user)).thenReturn(response);

        // WHEN
        UserResponseDTO userFound = userService.findByUsername(user.getUsername());

        // THEN
        assertThat(userFound)
                .extracting(UserResponseDTO::username, UserResponseDTO::email)
                .containsExactly(user.getUsername(), user.getEmail());

        verify(userRepository).findByUsername(user.getUsername());
        verify(userMapper).generateDTOFromUser(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when username does not exist")
    void shouldThrowException_whenUsernameNotFound() {
        // GIVEN
        // Empty Optional for username = "Gianni"
        String username = "Gianni";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // WHEN
        assertThatThrownBy(() -> userService.findByUsername(username))
                .isInstanceOf(UserNotFoundException.class) // Verifica il tipo di eccezione
                .hasMessageContaining(MSG_NOT_FOUND)
                .hasMessageContaining(username); // Verifica il messaggio esatto

        // Verify Mapper never used
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should return user when searching by existing email")
    void shouldReturnUser_whenEmailExists() {
        // GIVEN
        User user = createEntity();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.generateDTOFromUser(user)).thenReturn(createResponse());

        // WHEN
        UserResponseDTO userFound = userService.findByEmail(user.getEmail());

        // THEN
        assertThat(userFound)
                .extracting(UserResponseDTO::username, UserResponseDTO::email)
                .containsExactly(user.getUsername(), user.getEmail());

        verify(userRepository).findByEmail(user.getEmail());
        verify(userMapper).generateDTOFromUser(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when email does not exist")
    void shouldThrowException_whenEmailNotFound() {
        // GIVEN
        String email = "email1@test.it";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        // WHEN
        assertThatThrownBy(() -> userService.findByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(MSG_NOT_FOUND)
                .hasMessageContaining(email);

        // VERIFY
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should delete user successfully when user exists")
    void shouldDeleteUser_whenUserExists() {

        when(userRepository.existsById(VALID_ID)).thenReturn(true);

        userService.deleteUser(VALID_ID);

        verify(userRepository).existsById(VALID_ID);
        verify(userRepository).deleteById(VALID_ID);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting a non-existent user")
    void shouldThrowException_whenDeletingNonExistentUser() {
        // GIVEN
        when(userRepository.existsById(NON_EXISTENT_ID)).thenReturn(false);

        // WHEN
        assertThatThrownBy(() -> userService.deleteUser(NON_EXISTENT_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(MSG_NOT_FOUND);

        // THEN E VERIFY
        // Verifichiamo che existsById sia stato chiamato
        verify(userRepository).existsById(NON_EXISTENT_ID);
        // E che deleteById NON sia mai stato chiamato
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should partially update user fields via PATCH")
    void shouldPartiallyUpdateUser_whenFieldsAreProvided() {
        User existingUser = createEntity();
        String newFirstName = "Luigi";
        String newEmail = "newemail@test.it";
        String newPhone = "333123";

        UserPatchRequestDTO request = new UserPatchRequestDTO(newFirstName, null, newEmail, newPhone);
        UserResponseDTO expectedResponse = new UserResponseDTO(
                existingUser.getId(),
                existingUser.getUsername(), // Invariato
                newEmail, // Aggiornato
                newFirstName, // Aggiornato
                existingUser.getLastName(), // Invariato
                newPhone, // Aggiornato
                existingUser.getUserRole(),
                null);

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.generateDTOFromUser(existingUser)).thenReturn(expectedResponse);

        // 2. ACT
        UserResponseDTO result = userService.patchUser(existingUser.getId(), request);

        // 3. ASSERT — ENTITY
        assertThat(existingUser.getFirstName()).isEqualTo(newFirstName);
        assertThat(existingUser.getPhone()).isEqualTo(newPhone);
        assertThat(existingUser.getEmail()).isEqualTo(newEmail);
        assertThat(existingUser.getLastName()).isEqualTo("Rossi");

        // 4. ASSERT — DTO
        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo(newFirstName);
        assertThat(result.lastName()).isEqualTo("Rossi"); // NOT overwritten
        assertThat(result.phone()).isEqualTo(newPhone);
        assertThat(result.email()).isEqualTo(newEmail);

        // 5. VERIFY
        verify(userRepository).findById(existingUser.getId());
        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository).save(any(User.class));
        verify(userMapper).generateDTOFromUser(existingUser);
    }

    @Test
    @DisplayName("Should skip email uniqueness check when PATCH email matches current value")
    void shouldSkipEmailCheck_whenPatchEmailMatchesCurrent() {

        User existingUser = createEntity();

        UserPatchRequestDTO request = new UserPatchRequestDTO(null, null, "mario@test.it", null);

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.generateDTOFromUser(any(User.class))).thenReturn(createResponse());

        // ACT
        UserResponseDTO result = userService.patchUser(existingUser.getId(), request);

        // ASSERT — ENTITY
        assertThat(existingUser.getEmail()).isEqualTo(request.email());

        // ASSERT — DTO
        assertThat(result.email()).isEqualTo(request.email());

        // VERIFY
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
        verify(userMapper).generateDTOFromUser(existingUser);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when PATCH email is already associated")
    void shouldThrowException_whenPatchEmailAlreadyAssociated() {

        User existingUser = createEntity();

        UserPatchRequestDTO request = new UserPatchRequestDTO(null, null, "new@email.it", null);

        when(userRepository.findById(existingUser.getId()))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.patchUser(existingUser.getId(), request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already associated");

        // VERIFY
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).generateDTOFromUser(any());
    }

    @Test
    @DisplayName("Should apply no changes when all PATCH fields are null")
    void shouldApplyNoChanges_whenAllPatchFieldsAreNull() {

        User existingUser = createEntity();

        UserPatchRequestDTO request = new UserPatchRequestDTO(null, null, null, null);

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.generateDTOFromUser(any(User.class))).thenReturn(createResponse());

        // ACT
        userService.patchUser(existingUser.getId(), request);

        // ASSERT — ENTITY
        assertThat(existingUser.getFirstName()).isEqualTo("Mario");
        assertThat(existingUser.getLastName()).isEqualTo("Rossi");
        assertThat(existingUser.getEmail()).isEqualTo("mario@test.it");
        assertThat(existingUser.getPhone()).isEqualTo("123");

        // VERIFY
        verify(userRepository).save(any(User.class));
        verify(userMapper).generateDTOFromUser(existingUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when patching a non-existent user")
    void shouldThrowException_whenPatchingNonExistentUser() {

        UserPatchRequestDTO request = new UserPatchRequestDTO("Luigi", null, null, "333");

        when(userRepository.findById(NON_EXISTENT_ID))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.patchUser(NON_EXISTENT_ID, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(MSG_NOT_FOUND);
        // VERIFY
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).generateDTOFromUser(any());
    }

    @Test
    @DisplayName("Should fully replace user via PUT when all validations pass")
    void shouldReplaceUser_whenPutRequestIsValid() {
        // GIVEN
        User existingUser = createEntity();
        UserPutRequestDTO request = createPutRequest();

        UserPutResponseDTO expectedResponse = createPutResponse();

        // MOCK repository checks
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        // MOCK mapper update
        when(userMapper.updateUserFromPutDTO(request, existingUser)).thenReturn(existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.generatePutResponseFromUser(any(User.class))).thenReturn(expectedResponse);

        // WHEN
        UserPutResponseDTO response = userService.putUser(existingUser.getId(), request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(existingUser.getId());
        assertThat(response.username()).isEqualTo(request.username());
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.firstName()).isEqualTo(request.firstName());
        assertThat(response.lastName()).isEqualTo(request.lastName());
        assertThat(response.phone()).isEqualTo(request.phone());

        // VERIFY
        verify(userRepository).save(existingUser);
        verify(userMapper).generatePutResponseFromUser(existingUser);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when PUT username is already taken")
    void shouldThrowException_whenPutUsernameAlreadyExists() {
        // Arrange
        UserPutRequestDTO requestDTO = createPutRequest();
        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.putUser(VALID_ID, requestDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage(MSG_USERNAME_TAKEN);

        verify(userRepository, never()).save(any());
        verify(userRepository).existsByUsername(requestDTO.username());
        verify(userRepository, never()).existsByEmail(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when PUT email is already associated")
    void shouldThrowException_whenPutEmailAlreadyExists() {
        // GIVEN
        UserPutRequestDTO requestDTO = createPutRequest();

        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(false);
        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.putUser(VALID_ID, requestDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage(MSG_EMAIL_TAKEN);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when PUT targets a non-existent user")
    void shouldThrowException_whenPutUserNotFound() {
        // GIVEN
        UserPutRequestDTO requestDTO = createPutRequest();
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.putUser(NON_EXISTENT_ID, requestDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(MSG_NOT_FOUND);

        // Verify
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }
}