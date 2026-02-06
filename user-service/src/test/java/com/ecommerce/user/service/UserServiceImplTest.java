package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.impl.UserServiceImpl;
import com.ecommerce.user.util.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserOk() {
        // GIVEN
        UserRequestDTO request = new UserRequestDTO(
                "mario88",
                "mario@test.it",
                "pass",
                "Mario",
                "Rossi",
                "123"
        );

        User newUser = new User();

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.generateUserFromDTO(request)).thenReturn(newUser);

        // save() returns the same entity (JPA-like behavior)
        // realistic mapper entity based
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.generateDTOFromUser(newUser))
                .thenReturn(new UserResponseDTO(
                        1L,
                        request.username(),
                        request.email(),
                        request.firstName(),
                        request.lastName(),
                        request.phone(),
                        User.UserRole.CUSTOMER,
                        null
                ));

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

    // SCENARIO 1: username already exist
    @Test
    void createUserKo_WhenUsernameExists_ShouldThrowException() {
        // GIVEN
        UserRequestDTO request = new UserRequestDTO(
                "mario_rossi",
                "mario@email.com",
                "pass123",
                "Mario",
                "Rossi",
                "123");

        // Mock: userRepository username exist
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already in use");

        // VERIFY
        verify(userRepository).existsByUsername(request.username());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userMapper, never()).generateUserFromDTO(any());
        verify(userRepository, never()).save(any());
    }

    // SCENARIO 2: Free Username, Email existing
    @Test
    void createUserKo_WhenEmailExists_ShouldThrowException() {
        // GIVEN
        UserRequestDTO request = new UserRequestDTO("mario_rossi", "mario@email.com", "pass123", "Mario", "Rossi", "123");

        // Mock: Free Username, Email existing
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already associated");

        // VERIFY
        verify(userRepository).existsByUsername(request.username());
        verify(userRepository).existsByEmail(request.email());

        // Let's verify that, although the preliminary checks have partially passed,
        // the entity has not been created.
        verify(userMapper, never()).generateUserFromDTO(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void getAllUsersOk(){

        //GIVEN
        User user1 = new User(1L, "duplicato", "email1@test.it", "pass", "A", "B", "1", User.UserRole.CUSTOMER);
        User user2 = new User(2L, "gianni", "email2@test.it", "pass", "A", "B", "1", User.UserRole.CUSTOMER);
        UserResponseDTO responseDTO1 = new UserResponseDTO(user1.getId(),user1.getUsername(),user1.getEmail(),user1.getFirstName(), user1.getLastName(), user1.getPhone(), user1.getUserRole(),null);
        UserResponseDTO responseDTO2 = new UserResponseDTO(user2.getId(),user2.getUsername(),user2.getEmail(),user2.getFirstName(), user2.getLastName(), user2.getPhone(), user2.getUserRole(),null);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1,user2));
        when(userMapper.generateDTOFromUser(user1)).thenReturn(responseDTO1);
        when(userMapper.generateDTOFromUser(user2)).thenReturn(responseDTO2);

        //WHEN
        List<UserResponseDTO> result = userService.getAllUsers();

        //THEN
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserResponseDTO::username).containsExactlyInAnyOrder(responseDTO1.username(),responseDTO2.username());

        //VERIFY
        verify(userRepository).findAll();
        verify(userMapper,times(2)).generateDTOFromUser(any(User.class));
    }

    @Test
    void getAllUsersOk_WhenNoUsersExist_ShouldReturnEmptyList() {
        // GIVEN
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // WHEN
        List<UserResponseDTO> result = userService.getAllUsers();

        // THEN
        assertThat(result).isEmpty();
        verify(userRepository).findAll();
        verifyNoInteractions(userMapper); //nothing to map if the list is empty
    }

    @Test
    void findByIdOK(){
        //GIVEN
        User user1 = new User(
                1L,
                "duplicato",
                "email1@test.it",
                "pass",
                "A",
                "B",
                "333333333",
                 User.UserRole.CUSTOMER);

        UserResponseDTO responseDTO1 = new UserResponseDTO(
                user1.getId(),
                user1.getUsername(),
                user1.getEmail(),
                user1.getFirstName(),
                user1.getLastName(),
                user1.getPhone(),
                user1.getUserRole(),
                null);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userMapper.generateDTOFromUser(user1)).thenReturn(responseDTO1);

        //WHEN
        UserResponseDTO userFound = userService.findById(user1.getId());

        //THEN
        assertThat(userFound)
                .extracting(UserResponseDTO::id, UserResponseDTO::firstName)
                .containsExactly(user1.getId(), user1.getFirstName());

        verify(userRepository).findById(user1.getId());
        verify(userMapper).generateDTOFromUser(user1);
    }

    @Test
    void findByIdKo_UserNotFound_ShouldThrowException(){
        //GIVEN
        //Empty Optional for userId = 100L
        Long userId = 100L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(UserNotFoundException.class) // Check the exception type
                .hasMessage("User with id " + userId + " not found"); // Check if the message is correct

        // Verify Mapper never used
        verifyNoInteractions(userMapper);
    }

    @Test
    void findByUsernameOK(){
        //GIVEN
        User user1 = new User(
                1L,
                "duplicato",
                "email1@test.it",
                "pass",
                "A",
                "B",
                "333333333",
                User.UserRole.CUSTOMER);

        UserResponseDTO responseDTO1 = new UserResponseDTO(
                user1.getId(),
                user1.getUsername(),
                user1.getEmail(),
                user1.getFirstName(),
                user1.getLastName(),
                user1.getPhone(),
                user1.getUserRole(),
                null);

        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userMapper.generateDTOFromUser(user1)).thenReturn(responseDTO1);

        //WHEN
        UserResponseDTO userFound = userService.findByUsername(user1.getUsername());

        //THEN
        assertThat(userFound)
                .extracting(UserResponseDTO::username, UserResponseDTO::email)
                .containsExactly(user1.getUsername(), user1.getEmail());

        verify(userRepository).findByUsername(user1.getUsername());
        verify(userMapper).generateDTOFromUser(user1);
    }

    @Test
    void findByUsernameKo_UserNotFound_ShouldThrowException(){
        //GIVEN
        //Empty Optional for username = "Gianni"
        String username = "Gianni";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // WHEN
        assertThatThrownBy(() -> userService.findByUsername(username))
                .isInstanceOf(UserNotFoundException.class) // Verifica il tipo di eccezione
                .hasMessage("User with username " + username + " not found"); // Verifica il messaggio esatto

        // Verify Mapper never used
        verifyNoInteractions(userMapper);
    }

    @Test
    void findByEmailOK(){
        //GIVEN
        User user1 = new User(
                1L,
                "Pablo",
                "email1@test.it",
                "pass",
                "A",
                "B",
                "333333333",
                User.UserRole.CUSTOMER);

        UserResponseDTO responseDTO1 = new UserResponseDTO(
                user1.getId(),
                user1.getUsername(),
                user1.getEmail(),
                user1.getFirstName(),
                user1.getLastName(),
                user1.getPhone(),
                user1.getUserRole(),
                null);

        when(userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));
        when(userMapper.generateDTOFromUser(user1)).thenReturn(responseDTO1);

        //WHEN
        UserResponseDTO userFound = userService.findByEmail(user1.getEmail());

        //THEN
        assertThat(userFound)
                .extracting(UserResponseDTO::username, UserResponseDTO::email)
                .containsExactly(user1.getUsername(), user1.getEmail());

        verify(userRepository).findByEmail(user1.getEmail());
        verify(userMapper).generateDTOFromUser(user1);
    }

    @Test
    void findByEmailKo_UserNotFound_ShouldThrowException(){
        //GIVEN
        String email = "email1@test.it";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        // WHEN
        assertThatThrownBy(() -> userService.findByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with email " + email + " not found");

        // VERIFY
        verifyNoInteractions(userMapper);
    }

    @Test
    void deleteUserOk(){
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUserKo_NoUserExist_ShouldThrowException() {
        // GIVEN
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // WHEN
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Cannot delete user with ID : " + userId);

        // THEN E VERIFY
        // Verifichiamo che existsById sia stato chiamato
        verify(userRepository).existsById(userId);
        // E che deleteById NON sia mai stato chiamato
        verify(userRepository, never()).deleteById(anyLong());
    }

        @Test
        void patchUserOk_PartialUpdate() {
            User existingUser = new User(
                    1L,
                    "duplicato",
                    "email1@test.it",
                    "pass",
                    "A",
                    "B",
                    "1",
                    User.UserRole.CUSTOMER
            );

            UserPatchRequestDTO request =
                    new UserPatchRequestDTO("Luigi", null, "newemail@test.it", "333123");

            when(userRepository.findById(existingUser.getId()))
                    .thenReturn(Optional.of(existingUser));

            when(userRepository.save(any(User.class)))
                    .thenAnswer(i -> i.getArgument(0));

            // realistic mapper entity based
            when(userMapper.generateDTOFromUser(any(User.class)))
                    .thenAnswer(i -> {
                        User u = i.getArgument(0);
                        return new UserResponseDTO(
                                u.getId(),
                                u.getUsername(),
                                u.getEmail(),
                                u.getFirstName(),
                                u.getLastName(),
                                u.getPhone(),
                                u.getUserRole(),
                                null
                        );
                    });

            // 2. ACT
            UserResponseDTO result = userService.patchUser(existingUser.getId(), request);

            // 3. ASSERT — ENTITY
            assertThat(existingUser.getFirstName()).isEqualTo(request.firstName());
            assertThat(existingUser.getPhone()).isEqualTo(request.phone());
            assertThat(existingUser.getEmail()).isEqualTo(request.email());

            // 4. ASSERT — DTO
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingUser.getId());
            assertThat(result.firstName()).isEqualTo(request.firstName());
            assertThat(result.lastName()).isEqualTo(existingUser.getLastName()); //NOT overwritten
            assertThat(result.phone()).isEqualTo(request.phone());
            assertThat(result.email()).isEqualTo(request.email());

            // 5. VERIFY
            verify(userRepository).findById(existingUser.getId());
            verify(userRepository).existsByEmail(request.email());
            verify(userRepository).save(any(User.class));
            verify(userMapper).generateDTOFromUser(existingUser);
        }

    @Test
    void patchUserOk_EmailSameAsCurrent() {

        User existingUser = new User(
                1L,
                "Mario",
                "email@test.it",
                "pass",
                "Rossi",
                "J",
                "123",
                User.UserRole.CUSTOMER
        );

        UserPatchRequestDTO request =
                new UserPatchRequestDTO(null, null, "email@test.it", null);

        when(userRepository.findById(existingUser.getId()))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));

        // realistic mapper entity based
        when(userMapper.generateDTOFromUser(any(User.class)))
                .thenAnswer(i -> {
                    User u = i.getArgument(0);
                    return new UserResponseDTO(
                            u.getId(),
                            u.getUsername(),
                            u.getEmail(),
                            u.getFirstName(),
                            u.getLastName(),
                            u.getPhone(),
                            u.getUserRole(),
                            null
                    );
                });

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
    void patchUserKo_EmailAlreadyAssociated_ShouldThrowException() {

        User existingUser = new User(
                1L,
                "Mario",
                "old@email.it",
                "pass",
                "Rossi",
                "Verdi",
                "123",
                User.UserRole.CUSTOMER
        );

        UserPatchRequestDTO request =
                new UserPatchRequestDTO(null, null, "new@email.it", null);

        when(userRepository.findById(existingUser.getId()))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() ->
                userService.patchUser(existingUser.getId(), request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already associated");

        // VERIFY
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).generateDTOFromUser(any());
    }

    @Test
    void patchUserOk_allFieldsNull_noChangesApplied() {

        User existingUser = new User(
                1L,
                "Marioxxx",
                "email@test.it",
                "pass",
                "Mario",
                "Rossi",
                "123",
                User.UserRole.CUSTOMER
        );

        UserPatchRequestDTO request =
                new UserPatchRequestDTO(null, null, null, null);

        when(userRepository.findById(existingUser.getId()))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));

        // realistic mapper entity based
        when(userMapper.generateDTOFromUser(any(User.class)))
                .thenAnswer(i -> {
                    User u = i.getArgument(0);
                    return new UserResponseDTO(
                            u.getId(),
                            u.getUsername(),
                            u.getEmail(),
                            u.getFirstName(),
                            u.getLastName(),
                            u.getPhone(),
                            u.getUserRole(),
                            null
                    );
                });

        // ACT
        UserResponseDTO result = userService.patchUser(existingUser.getId(), request);

        // ASSERT — ENTITY
        assertThat(existingUser.getFirstName()).isEqualTo("Mario");
        assertThat(existingUser.getLastName()).isEqualTo("Rossi");
        assertThat(existingUser.getEmail()).isEqualTo("email@test.it");
        assertThat(existingUser.getPhone()).isEqualTo("123");

        // ASSERT — DTO
        assertThat(result.firstName()).isEqualTo(existingUser.getFirstName());
        assertThat(result.lastName()).isEqualTo(existingUser.getLastName());
        assertThat(result.email()).isEqualTo(existingUser.getEmail());
        assertThat(result.phone()).isEqualTo(existingUser.getPhone());

        // VERIFY
        verify(userRepository).save(any(User.class));
        verify(userMapper).generateDTOFromUser(existingUser);
    }

    @Test
    void patchUserKo_UserNotFound_ShouldThrowException() {

        Long userId = 99L;

        UserPatchRequestDTO request =
                new UserPatchRequestDTO("Luigi", null, null, "333");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() ->
                userService.patchUser(userId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User Not Found");

        // VERIFY
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).generateDTOFromUser(any());
    }

    @Test
    void putUserOk() {
        // GIVEN
        Long userId = 1L;

        UserPutRequestDTO requestDTO = new UserPutRequestDTO(
                "john_doe",
                "john.doe@email.com",
                "secretPassword",
                "John",
                "Doe",
                "123456789",
                true,
                User.UserRole.CUSTOMER
        );

        User existingUser = new User();
        existingUser.setId(userId);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername(requestDTO.username());
        updatedUser.setEmail(requestDTO.email());
        updatedUser.setFirstName(requestDTO.firstName());
        updatedUser.setLastName(requestDTO.lastName());
        updatedUser.setPhone(requestDTO.phone());
        updatedUser.setActive(requestDTO.active());
        updatedUser.setUserRole(requestDTO.userRole());

        // MOCK repository checks
        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(false);
        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // MOCK mapper update
        when(userMapper.updateUserFromPutDTO(requestDTO, existingUser))
                .thenReturn(updatedUser);

        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        // realistic mapper entity based
        when(userMapper.generatePutResponseFromUser(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    return new UserPutResponseDTO(
                            u.getId(),
                            u.getUsername(),
                            u.getEmail(),
                            u.getFirstName(),
                            u.getLastName(),
                            u.getPhone(),
                            u.isActive(),
                            u.getUserRole(),
                            null,
                            null
                    );
                });

        // WHEN
        UserPutResponseDTO response = userService.putUser(userId, requestDTO);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo(requestDTO.username());
        assertThat(response.email()).isEqualTo(requestDTO.email());
        assertThat(response.firstName()).isEqualTo(requestDTO.firstName());
        assertThat(response.lastName()).isEqualTo(requestDTO.lastName());
        assertThat(response.phone()).isEqualTo(requestDTO.phone());
        assertThat(response.active()).isEqualTo(requestDTO.active());
        assertThat(response.userRole()).isEqualTo(requestDTO.userRole());

        verify(userRepository).save(updatedUser);
    }

    @Test
    void putUser_UsernameExists_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        UserPutRequestDTO requestDTO = new UserPutRequestDTO("john_doe",
                "john.doe@email.com",
                "secretPassword",
                "John",
                "Doe",
                "123456789",
                true,
                User.UserRole.CUSTOMER);
        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.putUser(userId, requestDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already in use");

        verify(userRepository, never()).save(any());
        verify(userRepository).existsByUsername(requestDTO.username());
        verify(userRepository, never()).existsByEmail(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    void putUser_EmailExists_ShouldThrowException() {
        // GIVEN
        Long userId = 1L;
        UserPutRequestDTO requestDTO = new UserPutRequestDTO("john_doe",
                "john.doe@email.com",
                "secretPassword",
                "John",
                "Doe",
                "123456789",
                true,
                User.UserRole.CUSTOMER);
        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(false);
        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.putUser(userId, requestDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already associated");

        verify(userRepository, never()).save(any());
    }

    @Test
    void putUser_UserNotFound_ShouldThrowException() {
        // GIVEN
        Long userId = 99L;
        UserPutRequestDTO requestDTO = new UserPutRequestDTO("john_doe",
                "john.doe@email.com",
                "secretPassword",
                "John",
                "Doe",
                "123456789",
                true,
                User.UserRole.CUSTOMER);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.putUser(userId, requestDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User Not Found");

        // Verify
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }
}