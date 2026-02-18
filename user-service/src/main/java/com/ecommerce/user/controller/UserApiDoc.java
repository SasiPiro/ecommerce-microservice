package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI contract for the User Management API.
 * All Swagger/OpenAPI documentation lives here; the implementing controller
 * stays focused on Spring MVC routing and business logic.
 */
@Tag(name = "User Management", description = "Full CRUD operations for user accounts within the e-commerce platform. "
                + "Paginated collections follow the Spring Data Page contract.")
public interface UserApiDoc {

        // -------------------------------------------------------------------------
        // CREATE
        // -------------------------------------------------------------------------

        @Operation(summary = "Register a new user", description = "Creates a new user account. Both `username` and `email` must be unique. "
                        + "On success the response includes a `Location` header pointing to the new resource.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "User registered successfully", headers = @Header(name = "Location", description = "URI of the newly created user, e.g. `/api/v1/users/42`", schema = @Schema(type = "string", format = "uri")), content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation failed - request body is missing or malformed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/validation-error",
                                          "title": "Invalid input data",
                                          "status": 400,
                                          "detail": "Validation failed",
                                          "instance": "/api/v1/users",
                                          "errors": {
                                            "username": "Required Username",
                                            "email": "Invalid email format"
                                          },
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "409", description = "Conflict - email or username already registered", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/user-already-exists",
                                          "title": "Data conflict",
                                          "status": 409,
                                          "detail": "Email already exists",
                                          "instance": "/api/v1/users",
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """)))
        })
        ResponseEntity<UserResponseDTO> createUser(UserRequestDTO dto);

        // -------------------------------------------------------------------------
        // READ - collection
        // -------------------------------------------------------------------------

        @Operation(summary = "List all users (paginated)", description = "Returns a paginated list of users. Supports `page`, `size`, and `sort` query parameters.")
        @ApiResponse(responseCode = "200", description = "Page of users returned successfully")
        Page<UserResponseDTO> getAllUsers(@ParameterObject Pageable pageable);

        // -------------------------------------------------------------------------
        // READ - single resource
        // -------------------------------------------------------------------------

        @Operation(summary = "Get user by ID", description = "Retrieves a single user by their unique numeric identifier.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/user-not-found",
                                          "title": "Resource not found",
                                          "status": 404,
                                          "detail": "User not found",
                                          "instance": "/api/v1/users/999",
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """)))
        })
        UserResponseDTO getById(
                        @Parameter(description = "Unique user identifier", required = true, example = "42") Long id);

        // -------------------------------------------------------------------------
        // READ - search by username
        // -------------------------------------------------------------------------

        @Operation(summary = "Find user by username", description = "Performs an exact-match lookup on the `username` field.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "No user found with the given username", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/user-not-found",
                                          "title": "Resource not found",
                                          "status": 404,
                                          "detail": "User not found with username: unknown_user",
                                          "instance": "/api/v1/users/search-username",
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """)))
        })
        UserResponseDTO getByUsername(
                        @Parameter(description = "Exact username (case-sensitive)", required = true, example = "john_doe") String username);

        // -------------------------------------------------------------------------
        // UPDATE - full replacement (PUT)
        // -------------------------------------------------------------------------

        @Operation(summary = "Full update of a user (PUT)", description = "Replaces **all** mutable fields of the user identified by `id`. "
                        + "Every field in the request body is mandatory.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = UserPutResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/validation-error",
                                          "title": "Invalid input data",
                                          "status": 400,
                                          "detail": "Validation failed",
                                          "instance": "/api/v1/users/42",
                                          "errors": {
                                            "password": "size must be between 8 and 100"
                                          },
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/user-not-found",
                                          "title": "Resource not found",
                                          "status": 404,
                                          "detail": "User not found",
                                          "instance": "/api/v1/users/999",
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "409", description = "Conflict - email or username already taken", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/user-already-exists",
                                          "title": "Data conflict",
                                          "status": 409,
                                          "detail": "Username already exists",
                                          "instance": "/api/v1/users/42",
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """)))
        })
        UserPutResponseDTO updateFull(
                        @Parameter(description = "Unique user identifier", required = true, example = "42") Long id,
                        UserPutRequestDTO dto);

        // -------------------------------------------------------------------------
        // UPDATE - partial (PATCH)
        // -------------------------------------------------------------------------

        @Operation(summary = "Partial update of a user (PATCH)", description = "Updates **only the provided fields**. "
                        + "Allowed: `firstName`, `lastName`, `email`, `phone`.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User patched successfully", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/validation-error",
                                          "title": "Invalid input data",
                                          "status": 400,
                                          "detail": "Validation failed",
                                          "instance": "/api/v1/users/42",
                                          "errors": {
                                            "email": "Invalid email format"
                                          },
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/user-not-found",
                                          "title": "Resource not found",
                                          "status": 404,
                                          "detail": "User not found",
                                          "instance": "/api/v1/users/42",
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "409", description = "Conflict - email already taken", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/user-already-exists",
                                          "title": "Data conflict",
                                          "status": 409,
                                          "detail": "Email already exists",
                                          "instance": "/api/v1/users/42",
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """)))
        })
        UserResponseDTO updatePartial(
                        @Parameter(description = "Unique user identifier", required = true, example = "42") Long id,
                        UserPatchRequestDTO dto);

        // -------------------------------------------------------------------------
        // DELETE
        // -------------------------------------------------------------------------

        @Operation(summary = "Delete a user", description = "Permanently removes the user identified by `id`. This action is **irreversible**.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "User deleted successfully - no content returned"),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                                        {
                                          "type": "https://api.ecommerce.it/errors/user-not-found",
                                          "title": "Resource not found",
                                          "status": 404,
                                          "detail": "User not found",
                                          "instance": "/api/v1/users/999",
                                          "service": "user-service",
                                          "timestamp": "2025-06-01T10:30:00Z"
                                        }
                                        """)))
        })
        void delete(
                        @Parameter(description = "Unique user identifier", required = true, example = "42") Long id);
}
