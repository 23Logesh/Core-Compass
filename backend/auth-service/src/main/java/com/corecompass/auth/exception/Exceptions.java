//package com.corecompass.auth.exception;
//
//import com.corecompass.auth.dto.ApiResponse;
//import jakarta.validation.ConstraintViolationException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//// ─────────────────────────────────────────────────────────────
//// Custom Exception Classes
//// ─────────────────────────────────────────────────────────────
//
//class DuplicateResourceException extends RuntimeException {
//    private final String code;
//    public DuplicateResourceException(String code, String message) {
//        super(message);
//        this.code = code;
//    }
//    public String getCode() { return code; }
//}
//
//class InvalidCredentialsException extends RuntimeException {
//    public InvalidCredentialsException(String message) { super(message); }
//}
//
//class InvalidTokenException extends RuntimeException {
//    public InvalidTokenException(String message) { super(message); }
//}
//
//class AccountDisabledException extends RuntimeException {
//    public AccountDisabledException(String message) { super(message); }
//}
//
//class UserNotFoundException extends RuntimeException {
//    public UserNotFoundException(String message) { super(message); }
//}
//
//class AccessDeniedException extends RuntimeException {
//    public AccessDeniedException(String message) { super(message); }
//}
