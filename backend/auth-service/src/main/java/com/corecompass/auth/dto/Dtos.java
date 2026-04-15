//package com.corecompass.auth.dto;
//
//import jakarta.validation.constraints.*;
//import lombok.*;
//
//import java.time.Instant;
//import java.util.UUID;
//
//// ─────────────────────────────────────────────────────────────
//// REQUEST DTOs
//// ─────────────────────────────────────────────────────────────
//
//class RegisterRequest {
//    @Email(message = "Must be a valid email address")
//    @NotBlank(message = "Email is required")
//    public String email;
//
//    @NotBlank(message = "Password is required")
//    @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
//    public String password;
//
//    @NotBlank(message = "Name is required")
//    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
//    public String name;
//}
//
//class LoginRequest {
//    @Email(message = "Must be a valid email address")
//    @NotBlank(message = "Email is required")
//    public String email;
//
//    @NotBlank(message = "Password is required")
//    public String password;
//}
//
//class RefreshTokenRequest {
//    // Refresh token is sent as an HttpOnly cookie by the browser automatically.
//    // This DTO is a placeholder for cases where cookie isn't available (mobile).
//    public String refreshToken;
//}
//
//class UpdateProfileRequest {
//    @Size(min = 2, max = 100)
//    public String name;
//
//    @Size(max = 500)
//    public String avatarUrl;
//}
//
//class ChangePasswordRequest {
//    @NotBlank
//    public String currentPassword;
//
//    @NotBlank
//    @Size(min = 8, max = 100)
//    public String newPassword;
//}
//
//// ─────────────────────────────────────────────────────────────
//// RESPONSE DTOs
//// ─────────────────────────────────────────────────────────────
//
//class AuthResponse {
//    public String accessToken;
//    public String tokenType = "Bearer";
//    public long expiresIn;      // seconds
//    public UserDTO user;
//
//    // Factory
//    public static AuthResponse of(String accessToken, long expiresIn, UserDTO user) {
//        AuthResponse r = new AuthResponse();
//        r.accessToken = accessToken;
//        r.expiresIn   = expiresIn;
//        r.user         = user;
//        return r;
//    }
//}
//
//class UserDTO {
//    public UUID    id;
//    public String  email;
//    public String  name;
//    public String  role;
//    public String  avatarUrl;
//    public Instant createdAt;
//}
//
//// Standard API envelope
//class ApiResponse<T> {
//    public boolean success;
//    public T       data;
//    public String  message;
//    public Instant timestamp = Instant.now();
//
//    public static <T> ApiResponse<T> ok(T data, String message) {
//        ApiResponse<T> r = new ApiResponse<>();
//        r.success = true;
//        r.data    = data;
//        r.message = message;
//        return r;
//    }
//
//    public static <T> ApiResponse<T> error(String code, String message) {
//        ApiResponse<T> r = new ApiResponse<>();
//        r.success = false;
//        r.message = message;
//        return r;
//    }
//}
