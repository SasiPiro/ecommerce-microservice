package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.dto.ProductStockRequestDto;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * OpenAPI contract for the Product Management API.
 * All Swagger/OpenAPI documentation lives here; the implementing controller
 * stays focused on Spring MVC routing and business logic.
 */
@Tag(name = "Product Management", description = "Full CRUD operations for products within the e-commerce platform, "
        + "including stock management and search capabilities.")
public interface ProductApiDoc {

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Operation(summary = "Create a new product", description = "Creates a new product linked to an existing category. "
            + "The `name` must be unique and `categoryId` is mandatory. "
            + "On success the response includes a `Location` header pointing to the new resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully", headers = @Header(name = "Location", description = "URI of the newly created product, e.g. `/api/v1/products/12`", schema = @Schema(type = "string", format = "uri")), content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed - request body is missing or malformed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/validation-error",
                      "title": "Invalid input data",
                      "status": 400,
                      "detail": "Validation failed",
                      "instance": "/api/v1/products",
                      "errors": {
                        "name": "Name required",
                        "categoryId": "Category ID required"
                      },
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Referenced category not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/category-not-found",
                      "title": "Resource not found",
                      "status": 404,
                      "detail": "Category not found with provided ID",
                      "instance": "/api/v1/products",
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "Conflict - product name already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/product-already-exists",
                      "title": "Data conflict",
                      "status": 409,
                      "detail": "Product name already in use",
                      "instance": "/api/v1/products",
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """)))
    })
    ResponseEntity<ProductResponseDto> createProduct(ProductRequestDto dto);

    // -------------------------------------------------------------------------
    // READ - collection
    // -------------------------------------------------------------------------

    @Operation(summary = "List all products", description = "Returns the complete list of products, each including its nested category.")
    @ApiResponse(responseCode = "200", description = "List of products returned successfully")
    List<ProductResponseDto> getAllProducts();

    // -------------------------------------------------------------------------
    // READ - single resource
    // -------------------------------------------------------------------------

    @Operation(summary = "Get product by ID", description = "Retrieves a single product by its unique numeric identifier, including its nested category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found", content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/product-not-found",
                      "title": "Resource not found",
                      "status": 404,
                      "detail": "Product not found with provided ID",
                      "instance": "/api/v1/products/999",
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """)))
    })
    ProductResponseDto getById(
            @Parameter(description = "Unique product identifier", required = true, example = "12") Long id);

    // -------------------------------------------------------------------------
    // READ - search by name
    // -------------------------------------------------------------------------

    @Operation(summary = "Search products by name", description = "Returns products whose name contains the given keyword (case-insensitive).")
    @ApiResponse(responseCode = "200", description = "Search results returned successfully")
    List<ProductResponseDto> searchByName(
            @Parameter(description = "Keyword to search in product names", required = true, example = "laptop") String keyword);

    // -------------------------------------------------------------------------
    // READ - search by price range
    // -------------------------------------------------------------------------

    @Operation(summary = "Search products by price range", description = "Returns products whose price falls within the given range (inclusive).")
    @ApiResponse(responseCode = "200", description = "Search results returned successfully")
    List<ProductResponseDto> searchByPriceRange(
            @Parameter(description = "Minimum price", required = true, example = "10.00") BigDecimal min,
            @Parameter(description = "Maximum price", required = true, example = "100.00") BigDecimal max);

    // -------------------------------------------------------------------------
    // UPDATE - full replacement (PUT)
    // -------------------------------------------------------------------------

    @Operation(summary = "Full update of a product (PUT)", description = "Replaces **all** mutable fields of the product identified by `id`. "
            + "A valid `categoryId` is mandatory.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/validation-error",
                      "title": "Invalid input data",
                      "status": 400,
                      "detail": "Validation failed",
                      "instance": "/api/v1/products/12",
                      "errors": {
                        "price": "Price must be more than 0"
                      },
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Product or referenced category not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/product-not-found",
                      "title": "Resource not found",
                      "status": 404,
                      "detail": "Product not found with provided ID",
                      "instance": "/api/v1/products/999",
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "Conflict - product name already taken", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/product-already-exists",
                      "title": "Data conflict",
                      "status": 409,
                      "detail": "Product name already in use",
                      "instance": "/api/v1/products/12",
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """)))
    })
    ProductResponseDto updateProduct(
            @Parameter(description = "Unique product identifier", required = true, example = "12") Long id,
            ProductRequestDto dto);

    // -------------------------------------------------------------------------
    // UPDATE - partial stock (PATCH)
    // -------------------------------------------------------------------------

    @Operation(summary = "Update product stock (PATCH)", description = "Updates **only the stock** of the product identified by `id`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock updated successfully", content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/validation-error",
                      "title": "Invalid input data",
                      "status": 400,
                      "detail": "Validation failed",
                      "instance": "/api/v1/products/12/stock",
                      "errors": {
                        "stock": "New stock value required"
                      },
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/product-not-found",
                      "title": "Resource not found",
                      "status": 404,
                      "detail": "Product not found with provided ID",
                      "instance": "/api/v1/products/999/stock",
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """)))
    })
    ProductResponseDto patchStock(
            @Parameter(description = "Unique product identifier", required = true, example = "12") Long id,
            ProductStockRequestDto dto);

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Operation(summary = "Delete a product", description = "Permanently removes the product identified by `id`. This action is **irreversible**.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully - no content returned"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://api.ecommerce.it/errors/product-not-found",
                      "title": "Resource not found",
                      "status": 404,
                      "detail": "Product not found with provided ID",
                      "instance": "/api/v1/products/999",
                      "service": "product-service",
                      "timestamp": "2026-03-01T10:30:00Z"
                    }
                    """)))
    })
    void deleteProduct(
            @Parameter(description = "Unique product identifier", required = true, example = "12") Long id);
}
