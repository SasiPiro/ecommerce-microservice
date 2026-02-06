package com.ecommerce.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    public static UserAlreadyExistsException forUsername() {
        return new UserAlreadyExistsException("Username already in use");
    }
    public static UserAlreadyExistsException forEmail() {
        return new UserAlreadyExistsException("Email already associated");
    }
}
