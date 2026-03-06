package com.ecommerce.product.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }

    public static CategoryAlreadyExistsException forName() {
        return new CategoryAlreadyExistsException("Category name already in use");
    }
}
