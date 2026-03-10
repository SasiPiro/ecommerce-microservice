package com.ecommerce.product.controller;

import com.ecommerce.product.dto.CategoryRequestDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * OpenAPI contract for the Category Management API.
 * All Swagger/OpenAPI documentation lives here; the implementing controller
 * stays focused on Spring MVC routing and business logic.
 */
@Tag(name = "Category Management", description = "Full CRUD operations for product categories within the e-commerce platform.")
public interface CategoryApiDoc {

  // -------------------------------------------------------------------------
  // CREATE
  // -------------------------------------------------------------------------

  @Operation(summary = "Create a new category", description = "Creates a new product category. The `name` must be unique. "
      + "On success the response includes a `Location` header pointing to the new resource.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Category created successfully", headers = @Header(name = "Location", description = "URI of the newly created category, e.g. `/api/v1/categories/5`", schema = @Schema(type = "string", format = "uri")), content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed - request body is missing or malformed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
          {
            "type": "https://api.ecommerce.it/errors/validation-error",
            "title": "Invalid input data",
            "status": 400,
            "detail": "Validation failed",
            "instance": "/api/v1/categories",
            "errors": {
              "name": "Category name required"
            },
            "service": "product-service",
            "timestamp": "2026-03-01T10:30:00Z"
          }
          """))),
      @ApiResponse(responseCode = "409", description = "Conflict - category name already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
          {
            "type": "https://api.ecommerce.it/errors/category-already-exists",
            "title": "Data conflict",
            "status": 409,
            "detail": "Category name already in use",
            "instance": "/api/v1/categories",
            "service": "product-service",
            "timestamp": "2026-03-01T10:30:00Z"
          }
          """)))
  })
  ResponseEntity<CategoryResponseDto> createCategory(CategoryRequestDto dto);

  // -------------------------------------------------------------------------
  // READ - collection
  // -------------------------------------------------------------------------

  @Operation(summary = "List all categories (paginated)", description = "Returns a paginated list of product categories. Supports `page`, `size`, and `sort` query parameters.")
  @ApiResponse(responseCode = "200", description = "Page of categories returned successfully")
  Page<CategoryResponseDto> getAllCategories(@ParameterObject Pageable pageable);

  // -------------------------------------------------------------------------
  // READ - single resource
  // -------------------------------------------------------------------------

  @Operation(summary = "Get category by ID", description = "Retrieves a single category by its unique numeric identifier.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Category found", content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
          {
            "type": "https://api.ecommerce.it/errors/category-not-found",
            "title": "Resource not found",
            "status": 404,
            "detail": "Category not found with provided ID",
            "instance": "/api/v1/categories/999",
            "service": "product-service",
            "timestamp": "2026-03-01T10:30:00Z"
          }
          """)))
  })
  CategoryResponseDto getById(
      @Parameter(description = "Unique category identifier", required = true, example = "5") Long id);

  // -------------------------------------------------------------------------
  // UPDATE - full replacement (PUT)
  // -------------------------------------------------------------------------

  @Operation(summary = "Full update of a category (PUT)", description = "Replaces **all** mutable fields of the category identified by `id`.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Category updated successfully", content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
          {
            "type": "https://api.ecommerce.it/errors/validation-error",
            "title": "Invalid input data",
            "status": 400,
            "detail": "Validation failed",
            "instance": "/api/v1/categories/5",
            "errors": {
              "name": "Category name required"
            },
            "service": "product-service",
            "timestamp": "2026-03-01T10:30:00Z"
          }
          """))),
      @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
          {
            "type": "https://api.ecommerce.it/errors/category-not-found",
            "title": "Resource not found",
            "status": 404,
            "detail": "Category not found with provided ID",
            "instance": "/api/v1/categories/999",
            "service": "product-service",
            "timestamp": "2026-03-01T10:30:00Z"
          }
          """))),
      @ApiResponse(responseCode = "409", description = "Conflict - category name already taken", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
          {
            "type": "https://api.ecommerce.it/errors/category-already-exists",
            "title": "Data conflict",
            "status": 409,
            "detail": "Category name already in use",
            "instance": "/api/v1/categories/5",
            "service": "product-service",
            "timestamp": "2026-03-01T10:30:00Z"
          }
          """)))
  })
  CategoryResponseDto updateCategory(
      @Parameter(description = "Unique category identifier", required = true, example = "5") Long id,
      CategoryRequestDto dto);

  // -------------------------------------------------------------------------
  // DELETE
  // -------------------------------------------------------------------------

  @Operation(summary = "Delete a category", description = "Permanently removes the category identified by `id`. This action is **irreversible**.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Category deleted successfully - no content returned"),
      @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
          {
            "type": "https://api.ecommerce.it/errors/category-not-found",
            "title": "Resource not found",
            "status": 404,
            "detail": "Category not found with provided ID",
            "instance": "/api/v1/categories/999",
            "service": "product-service",
            "timestamp": "2026-03-01T10:30:00Z"
          }
          """)))
  })
  void deleteCategory(
      @Parameter(description = "Unique category identifier", required = true, example = "5") Long id);
}
