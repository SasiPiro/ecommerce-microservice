package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
        log.info("Creazione nuovo utente: {}", dto.username());
        UserResponseDTO response = userService.createUser(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    // --- 2. READ (DTO + @ResponseStatus opzionale) ---
    // Perché: 200 OK è il default. Restituire direttamente il DTO è "Clean Code".
    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponseDTO getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/search-username")
    public UserResponseDTO getByUsername(@RequestParam String username) {
        return userService.findByUsername(username);
    }

    // --- 3. UPDATE (DTO + @ResponseStatus) ---
    // Perché: Torniamo l'oggetto aggiornato. Lo status 200 è implicito.
    @PutMapping("/{id}")
    public UserPutResponseDTO updateFull(@PathVariable Long id, @Valid @RequestBody UserPutRequestDTO dto) {
        log.info("Update totale utente ID: {}", id);
        return userService.putUser(id, dto);
    }

    @PatchMapping("/{id}")
    public UserResponseDTO updatePartial(@PathVariable Long id, @RequestBody UserPatchRequestDTO dto) {
        log.info("Patch parziale utente ID: {}", id);
        return userService.patchUser(id, dto);
    }

    // --- 4. DELETE (void + @ResponseStatus) ---
    // Perché: Una cancellazione riuscita non deve tornare un body (204 No Content).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("Eliminazione utente ID: {}", id);
        userService.deleteUser(id);
    }
}
