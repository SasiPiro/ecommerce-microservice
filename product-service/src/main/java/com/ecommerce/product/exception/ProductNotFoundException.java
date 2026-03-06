package com.ecommerce.product.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public static ProductNotFoundException forId() {
        return new ProductNotFoundException("Product not found with provided ID");
    }
}
