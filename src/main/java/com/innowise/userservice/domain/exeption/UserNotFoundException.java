package com.innowise.userservice.domain.exeption;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }
    public UserNotFoundException(UUID userId) {
        super("User not found with userId: " + userId);
    }
}
