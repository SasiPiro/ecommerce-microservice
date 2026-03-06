package com.ecommerce.product.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }

    public static CategoryNotFoundException forId() {
        return new CategoryNotFoundException("Category not found with provided ID");
    }
}
