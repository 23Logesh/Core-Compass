package com.corecompass.auth.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) { super(message); }
}
