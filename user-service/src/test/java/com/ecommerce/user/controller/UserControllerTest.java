package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
// -> test config
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @MockitoBean
        private UserService userService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        // --- CONSTANTS ---
        private static final Long VALID_ID = 1L;
        private static final Long INVALID_ID = 999L;
        private static final String DEFAULT_USERNAME = "mario_rossi";
        private static final String DEFAULT_EMAIL = "mario@email.com";

        // Paths
        private static final String BASE_PATH = "/api/v1/users";
        private static final String ID_PATH = BASE_PATH + "/{id}";
        private static final String SEARCH_PATH = BASE_PATH + "/search-username";

        // Factory per POST
        private UserRequestDTO createRequest() {
                return new UserRequestDTO(DEFAULT_USERNAME, DEFAULT_EMAIL, "pass123", "Mario", "Rossi", "123");
        }

        // Factory per GET/PATCH Response
        private UserResponseDTO createResponse() {
                return new UserResponseDTO(VALID_ID, DEFAULT_USERNAME, DEFAULT_EMAIL, "Mario", "Rossi", "123",
                                User.UserRole.CUSTOMER, null);
        }

        // Factory per PUT Request
        private UserPutRequestDTO createPutRequest() {
                return new UserPutRequestDTO(DEFAULT_USERNAME, DEFAULT_EMAIL, "abc123$$", "Mario", "Rossi", "123", true,
                                User.UserRole.CUSTOMER);
        }

        // Factory per PUT Response
        private UserPutResponseDTO createPutResponse() {
                return new UserPutResponseDTO(VALID_ID, DEFAULT_USERNAME, DEFAULT_EMAIL, "Mario", "Rossi", "123", true,
                                User.UserRole.CUSTOMER, null, null);
        }

        // --- 1. CREATE (POST) ---

        @Test
        @DisplayName("POST /api/v1/users - Should return 201 Created and Location Header")
        void createUser_WithValidData_ReturnsCreated() throws Exception {

                when(userService.createUser(any(UserRequestDTO.class))).thenReturn(createResponse());

                // When & Then
                mockMvc.perform(post(BASE_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest())))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", containsString(BASE_PATH + "/" + VALID_ID))) // Verifica
                                                                                                                    // header
                                                                                                                    // Location
                                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                                .andExpect(jsonPath("$.username", is(DEFAULT_USERNAME)))
                                .andExpect(jsonPath("$.email", is(DEFAULT_EMAIL)));

                verify(userService).createUser(any(UserRequestDTO.class));
        }

        // --- POST CREATE ERRORS ---

        @Test
        @DisplayName("POST /api/v1/users - Should return 400 Bad Request when Validation Fails")
        void createUser_WithInvalidDTO_ReturnsBadRequest() throws Exception {
                // Given
                // Create a DTO with null or invalid fields to trigger @Valid
                UserRequestDTO invalidDto = new UserRequestDTO(null, "not-an-email", "", "", "", "");

                // When & Then
                mockMvc.perform(post(BASE_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto)))
                                .andExpect(status().isBadRequest()); // 400

                // Verify that the service has NEVER been called because validation blocked
                // everything before
                verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("POST /api/v1/users - Should return 409 Conflict when Username already exists")
        void createUser_WhenUsernameExists_ReturnsConflict() throws Exception {

                // simulation custom exception thrown
                when(userService.createUser(any(UserRequestDTO.class)))
                                .thenThrow(UserAlreadyExistsException.forUsername());

                // When & Then
                mockMvc.perform(post(BASE_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest())))
                                .andExpect(status().isConflict()); // 409
        }

        // --- 2. READ (GET) ---

        @Test
        @DisplayName("GET /api/v1/users - Should return 200 OK and list of users")
        void getAllUsers_ReturnsPage() throws Exception {
                // Given
                List<UserResponseDTO> userList = List.of(
                                createResponse(),
                                new UserResponseDTO(2L, "user2", "u2@test.com", "User", "Two", "222", null, null));

                PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("username").ascending());
                Page<UserResponseDTO> userPage = new PageImpl<>(userList, pageRequest, userList.size());

                // mock the service: it must receive any Pageable and return our page
                when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

                // When & Then
                mockMvc.perform(get(BASE_PATH)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "username,asc")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[0].username", is(DEFAULT_USERNAME)))
                                .andExpect(jsonPath("$.content[1].username", is("user2")))
                                .andExpect(jsonPath("$.content[*].email").isNotEmpty())
                                .andExpect(jsonPath("$.totalElements", is(2)))
                                .andExpect(jsonPath("$.totalPages", is(1)))
                                .andExpect(jsonPath("$.number", is(0))) // Pagina corrente
                                .andExpect(jsonPath("$.size", is(10))); // Dimensione pagina

                verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("GET /api/v1/users/{id} - Should return 200 OK when found")
        void getById_WhenUserExists_ReturnsUser() throws Exception {

                when(userService.findById(VALID_ID)).thenReturn(createResponse());

                // When & Then
                mockMvc.perform(get(ID_PATH, VALID_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                                .andExpect(jsonPath("$.username", is(DEFAULT_USERNAME)))
                                .andExpect(jsonPath("$.email", is(DEFAULT_EMAIL)));

                verify(userService).findById(VALID_ID);
        }

        @Test
        @DisplayName("GET /api/v1/users/search-username - Should return 200 OK with query param")
        void getByUsername_WhenUserExists_ReturnsUser() throws Exception {

                when(userService.findByUsername(DEFAULT_USERNAME)).thenReturn(createResponse());

                // When & Then
                mockMvc.perform(get(SEARCH_PATH)
                                .param("username", DEFAULT_USERNAME)) // Testiamo il @RequestParam
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username", is(DEFAULT_USERNAME)));

                verify(userService).findByUsername(DEFAULT_USERNAME);
        }

        // --- GET READ ERRORS ---

        @Test
        @DisplayName("GET /api/v1/users/{id} - Should return 404 Not Found when ID does not exist")
        void getById_WhenUserNotFound_ReturnsNotFound() throws Exception {

                when(userService.findById(INVALID_ID))
                                .thenThrow(UserNotFoundException.forId());

                // When & Then
                mockMvc.perform(get(ID_PATH, INVALID_ID))
                                .andExpect(status().isNotFound()); // 404
        }

        @Test
        @DisplayName("GET /api/v1/users/{id} - Should return 400 Bad Request for invalid ID format")
        void getById_WithInvalidIdType_ReturnsBadRequest() throws Exception {
                // When & Then
                // We pass a generic string "abc" where a Long is expected
                mockMvc.perform(get(BASE_PATH + "/abc"))
                                .andExpect(status().isBadRequest()); // 400 Type Mismatch
        }

        @Test
        @DisplayName("GET /api/v1/users/search-username - Should return 404 when Username not found")
        void getByUsername_WhenUserNotFound_ReturnsNotFound() throws Exception {
                // Given
                String unknownUser = "unknown";
                when(userService.findByUsername(unknownUser))
                                .thenThrow(UserNotFoundException.forUsername(unknownUser));

                // When & Then
                mockMvc.perform(get(SEARCH_PATH)
                                .param("username", unknownUser))
                                .andExpect(status().isNotFound()); // 404
        }

        @Test
        @DisplayName("GET /api/v1/users/search-username - Should return 400 when Parameter is missing")
        void getByUsername_WithoutParam_ReturnsBadRequest() throws Exception {
                // When & Then
                // We don't pass the ?username= parameter
                mockMvc.perform(get(SEARCH_PATH))
                                .andExpect(status().isBadRequest()); // 400 MissingServletRequestParameterException
        }

        // --- 3. UPDATE PUT ---

        @Test
        @DisplayName("PUT /api/v1/users/{id} - Should return 200 OK and updated DTO")
        void updateFull_WithValidData_ReturnsUpdatedUser() throws Exception {

                when(userService.putUser(eq(VALID_ID), any(UserPutRequestDTO.class))).thenReturn(createPutResponse());

                // When & Then
                mockMvc.perform(put(ID_PATH, VALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createPutRequest())))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(VALID_ID.intValue())))
                                .andExpect(jsonPath("$.username", is(DEFAULT_USERNAME)))
                                .andExpect(jsonPath("$.email", is(DEFAULT_EMAIL)));

                verify(userService).putUser(eq(VALID_ID), any(UserPutRequestDTO.class));
        }

        // --- 3. PUT UPDATE ERRORS ---

        @Test
        @DisplayName("PUT /api/v1/users/{id} - Should return 400 Bad Request when Validation Fails")
        void updateFull_WithInvalidDTO_ReturnsBadRequest() throws Exception {
                // Given : invalid email
                UserPutRequestDTO invalidDto = new UserPutRequestDTO(null, "invalid-email", null, null, null, null,
                                true, null);

                // When & Then
                mockMvc.perform(put(ID_PATH, VALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto)))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).putUser(any(), any());
        }

        @Test
        @DisplayName("PUT /api/v1/users/{id} - Should return 404 Not Found when updating non-existent user")
        void updateFull_WhenUserNotFound_ReturnsNotFound() throws Exception {

                when(userService.putUser(eq(INVALID_ID), any(UserPutRequestDTO.class)))
                                .thenThrow(UserNotFoundException.forId());

                // When & Then
                mockMvc.perform(put(ID_PATH, INVALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createPutRequest())))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/v1/users/{id} - Should return 409 Conflict on duplicate email")
        void updateFull_WhenEmailTaken_ReturnsConflict() throws Exception {

                when(userService.putUser(eq(VALID_ID), any(UserPutRequestDTO.class)))
                                .thenThrow(UserAlreadyExistsException.forEmail());

                // When & Then
                mockMvc.perform(put(ID_PATH, VALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createPutRequest())))
                                .andExpect(status().isConflict());
        }

        // --- 4. UPDATE PATCH ---

        @Test
        @DisplayName("PATCH /api/v1/users/{id} - Should return 200 OK")
        void updatePartial_WithValidData_ReturnsUpdatedUser() throws Exception {

                // Simulating patch only on the phone
                String expectedPhone = "555001199";
                UserPatchRequestDTO patchRequest = new UserPatchRequestDTO(null, null, null, expectedPhone);
                UserResponseDTO response = new UserResponseDTO(
                                VALID_ID,
                                DEFAULT_USERNAME,
                                "mario@test.com",
                                "Mario",
                                "Rossi",
                                expectedPhone,
                                User.UserRole.CUSTOMER,
                                null);

                when(userService.patchUser(eq(VALID_ID), any(UserPatchRequestDTO.class))).thenReturn(response);

                // When & Then
                mockMvc.perform(patch(ID_PATH, VALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phone", is(expectedPhone)));// Verifichiamo il campo aggiornato

                verify(userService).patchUser(eq(VALID_ID), any(UserPatchRequestDTO.class));
        }

        // --- 4. UPDATE PATCH ERRORS ---

        @Test
        @DisplayName("PATCH /api/v1/users/{id} - Should return 404 Not Found")
        void updatePartial_WhenUserNotFound_ReturnsNotFound() throws Exception {

                UserPatchRequestDTO dto = new UserPatchRequestDTO("name", null, null, null);

                when(userService.patchUser(eq(INVALID_ID), any(UserPatchRequestDTO.class)))
                                .thenThrow(UserNotFoundException.forId());

                // When & Then
                mockMvc.perform(patch(ID_PATH, INVALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PATCH /api/v1/users/{id} - Should return 409 Conflict when updating to an existing email")
        void updatePartial_WhenEmailAlreadyExists_ReturnsConflict() throws Exception {

                // Given : valid email but already associated
                UserPatchRequestDTO patchRequest = new UserPatchRequestDTO(null, null, "occupied@test.com", null);

                // simulating that the service throws the exception because the email already
                // exists
                when(userService.patchUser(eq(VALID_ID), any(UserPatchRequestDTO.class)))
                                .thenThrow(UserAlreadyExistsException.forEmail());

                // When & Then
                mockMvc.perform(patch(ID_PATH, VALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andExpect(status().isConflict()); // Ci aspettiamo 409

                verify(userService).patchUser(eq(VALID_ID), any(UserPatchRequestDTO.class));
        }

        @Test
        @DisplayName("PATCH /api/v1/users/{id} - Should return 400 Bad Request for malformed JSON")
        void updatePartial_WithMalformedJson_ReturnsBadRequest() throws Exception {
                // Given : broken Json
                String brokenJson = "{ \"email\": \"mancano-le-virgolette-di-chiusura... ";

                // When & Then
                mockMvc.perform(patch(ID_PATH, VALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(brokenJson))
                                .andExpect(status().isBadRequest()); // 400 Bad Request

                // verify the service is NEVER called
                verify(userService, never()).patchUser(any(), any());
        }

        // --- 5. DELETE ---

        @Test
        @DisplayName("DELETE /api/v1/users/{id} - Should return 204 No Content")
        void delete_WhenUserExists_ReturnsNoContent() throws Exception {

                doNothing().when(userService).deleteUser(VALID_ID);

                // When & Then
                mockMvc.perform(delete(ID_PATH, VALID_ID))
                                .andExpect(status().isNoContent()); // 204 No Content

                verify(userService).deleteUser(VALID_ID);
        }

        // --- 5. DELETE ERRORS ---

        @Test
        @DisplayName("DELETE /api/v1/users/{id} - Should return 404 Not Found if user doesn't exist")
        void delete_WhenUserNotFound_ReturnsNotFound() throws Exception {

                // throws UserNotFoundException if !existsById
                doThrow(UserNotFoundException.forId()).when(userService).deleteUser(INVALID_ID);

                // When & Then
                mockMvc.perform(delete(ID_PATH, INVALID_ID))
                                .andExpect(status().isNotFound()); // 404

                verify(userService).deleteUser(INVALID_ID);
        }
}
