package com.ecommerce.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    public static UserNotFoundException forId(Long id) {
        return new UserNotFoundException("User with id " + id + " not found");
    }
    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("User with username " + username + " not found");
    }
    public static UserNotFoundException forEmail(String email) {
        return new UserNotFoundException("User with email " + email + " not found");
    }
}
