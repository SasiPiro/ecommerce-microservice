package com.ecommerce.user.integration;

import com.ecommerce.user.bin.UserServiceApplication;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = UserServiceApplication.class)
@ActiveProfiles("test")
@DisplayName("Integration Test (H2) - User Controller")
class UserIntegrationH2Test {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;

    @Value("${test.server.host:localhost}")
    private String host;


    @BeforeEach
    void setUp() {
        // CLEAN SLATE
        restTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("http://" + host + ":" + port));
        baseUrl = "/api/v1/users";
        userRepository.deleteAll();
    }

    // --- HELPER FACTORY PER TEST ---
    private UserRequestDTO createRequest() {
        return new UserRequestDTO("mario_rossi", "mario@email.com", "pass123", "Mario", "Rossi", "123");
    }

    @Test
    @DisplayName("FULL LIFECYCLE - Create, Get, Patch, Delete")
    void fullUserLifecycle_IntegrationFlow() {
        // --- STEP 1: CREATE ---
        UserRequestDTO createReq = createRequest();
        ResponseEntity<UserResponseDTO> createRes = restTemplate.postForEntity(baseUrl, createReq, UserResponseDTO.class);
        Long userId = Objects.requireNonNull(createRes.getBody()).id();

        assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createRes.getBody()).isNotNull();
        assertThat(createRes.getBody().firstName()).isEqualTo(createReq.firstName());

        // --- STEP 2: GET BY ID ---
        ResponseEntity<UserResponseDTO> getRes = restTemplate.getForEntity(baseUrl + "/" + userId, UserResponseDTO.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getRes.getBody()).isNotNull();
        assertThat(getRes.getBody().firstName()).isEqualTo("Mario");

        // --- STEP 3: PATCH (Update Phone only) ---
        String newPhone = "987654321";
        UserPatchRequestDTO patchReq = new UserPatchRequestDTO(null, null, null, newPhone);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserPatchRequestDTO> patchEntity = new HttpEntity<>(patchReq, headers);

        ResponseEntity<UserResponseDTO> patchRes = restTemplate.exchange(
                baseUrl + "/" + userId, HttpMethod.PATCH, patchEntity, UserResponseDTO.class);

        assertThat(patchRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchRes.getBody()).isNotNull();
        assertThat(patchRes.getBody().phone()).isEqualTo(newPhone);
        assertThat(patchRes.getBody().firstName()).isEqualTo("Mario"); // Non deve essere cambiato

        // --- STEP 4: DELETE ---
        restTemplate.delete(baseUrl + "/" + userId);


        ResponseEntity<ProblemDetail> notFoundRes = restTemplate.getForEntity(baseUrl + "/" + userId, ProblemDetail.class);
        assertThat(notFoundRes.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Error - Should return 409 Conflict when username already exists")
    void createUser_DuplicateUsername_Returns409ProblemDetail() {
        // 1. GIVEN: Creiamo un utente
        UserRequestDTO request = createRequest();
        restTemplate.postForEntity(baseUrl, request, UserResponseDTO.class);

        // 2. WHEN: Proviamo a ricrearlo
        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
                baseUrl, request, ProblemDetail.class);

        // 3. ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Data conflict");
        assertThat(body.getDetail()).contains("Username already in use"); // Messaggio lanciato dal Service
        // Verifica le proprietà custom che hai aggiunto nel tuo Handler
        assertThat(body.getProperties()).containsEntry("service", "user-service");
        assertThat(body.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Error - Should return 400 and list of field errors when validation fails")
    void createUser_InvalidData_Returns400WithErrorsMap() {
        // 1. GIVEN: Request invalida (username corto, email malformata)
        UserRequestDTO invalidRequest = new UserRequestDTO(
                "ab",           // min 3
                "not-an-email", // @Email
                "123",          // min 6
                "", "", ""      // campi vuoti
        );

        // 2. WHEN
        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
                baseUrl , invalidRequest, ProblemDetail.class);

        // 3. ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Invalid input data");

        // Verifichiamo la mappa degli errori che hai creato nell'Handler
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.getProperties().get("errors");

        assertThat(errors)
                .containsKeys("username", "email", "password")
                .containsValue("Invalid email format"); // Uno dei tuoi messaggi nei DTO
    }

    @Test
    @DisplayName("Error - Should return 404 when patching non-existent user")
    void patchUser_NotFound_Returns404() {
        // GIVEN
        Long nonExistentId = 9999L;
        UserPatchRequestDTO patch = new UserPatchRequestDTO(null, null, null, "333123456");

        // WHEN
        ResponseEntity<ProblemDetail> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.PATCH,
                new HttpEntity<>(patch),
                ProblemDetail.class
        );

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Resource not found");
    }
}
