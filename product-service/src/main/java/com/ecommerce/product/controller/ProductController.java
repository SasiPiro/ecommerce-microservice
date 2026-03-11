package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.dto.ProductStockRequestDto;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductApiDoc {

    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto dto) {
        log.info("Creating product: {}", dto.name());
        ProductResponseDto response = productService.createProduct(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    @GetMapping
    public List<ProductResponseDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @Override
    @GetMapping("/{id}")
    public ProductResponseDto getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @Override
    @GetMapping("/search")
    public List<ProductResponseDto> searchByName(@RequestParam String keyword) {
        return productService.searchByName(keyword);
    }

    @Override
    @GetMapping("/price-range")
    public List<ProductResponseDto> searchByPriceRange(@RequestParam BigDecimal min,
                                                       @RequestParam BigDecimal max) {
        return productService.searchByPriceRange(min, max);
    }

    @Override
    @PutMapping("/{id}")
    public ProductResponseDto updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDto dto) {
        log.info("Full update (PUT) - product id: {}", id);
        return productService.updateProduct(id, dto);
    }

    @Override
    @PatchMapping("/{id}/stock")
    public ProductResponseDto patchStock(@PathVariable Long id, @Valid @RequestBody ProductStockRequestDto dto) {
        log.info("Patching stock - product id: {}", id);
        return productService.patchStock(id, dto);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        log.info("Deleting product id: {}", id);
        productService.deleteProduct(id);
    }
}
