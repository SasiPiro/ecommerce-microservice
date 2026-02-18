package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserApiDoc {

        private final UserService userService;
        private static final Logger log = LoggerFactory.getLogger(UserController.class);

        public UserController(UserService userService) {
                this.userService = userService;
        }

        @Override
        @PostMapping
        public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
                log.info("Creating user: {}", dto.username());
                UserResponseDTO response = userService.createUser(dto);
                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}").buildAndExpand(response.id()).toUri();
                return ResponseEntity.created(location).body(response);
        }

        @Override
        @GetMapping
        public Page<UserResponseDTO> getAllUsers(@PageableDefault(sort = "id") Pageable pageable) {
                return userService.getAllUsers(pageable);
        }

        @Override
        @GetMapping("/{id}")
        public UserResponseDTO getById(@PathVariable Long id) {
                return userService.findById(id);
        }

        @Override
        @GetMapping("/search-username")
        public UserResponseDTO getByUsername(@RequestParam String username) {
                return userService.findByUsername(username);
        }

        @Override
        @PutMapping("/{id}")
        public UserPutResponseDTO updateFull(@PathVariable Long id, @Valid @RequestBody UserPutRequestDTO dto) {
                log.info("Full update (PUT) - user id: {}", id);
                return userService.putUser(id, dto);
        }

        @Override
        @PatchMapping("/{id}")
        public UserResponseDTO updatePartial(@PathVariable Long id, @Valid @RequestBody UserPatchRequestDTO dto) {
                log.info("Partial update (PATCH) - user id: {}", id);
                return userService.patchUser(id, dto);
        }

        @Override
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
                log.info("Deleting user id: {}", id);
                userService.deleteUser(id);
        }
}
