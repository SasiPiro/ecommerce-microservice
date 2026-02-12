package com.ecommerce.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    public static UserNotFoundException forId() {
        return new UserNotFoundException("User not found with provided ID");
    }
    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("User not found with username : " + username);
    }
    public static UserNotFoundException forEmail(String email) {
        return new UserNotFoundException("User not found with email : " + email);
    }
}
