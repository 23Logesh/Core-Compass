package com.corecompass.auth.controller;

import com.corecompass.auth.dto.*;
import com.corecompass.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Auth Controller — handles all authentication endpoints.
 *
 * All routes are public at the web layer (gateway passes them through).
 * Protected routes (/me, /logout) receive X-User-Id from the gateway JWT filter.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── POST /api/v1/auth/register ─────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.register(request, response);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(auth, "Registration successful. Welcome to CoreCompass!"));
    }

    // ── POST /api/v1/auth/login ────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.ok(auth, "Login successful"));
    }

    // ── POST /api/v1/auth/refresh ──────────────────────────────────────────
    // Refresh token comes as HttpOnly cookie (auto-sent by browser).
    // Also accepts it in the request body for mobile clients.
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1. Try HttpOnly cookie first
        String rawToken = extractRefreshTokenFromCookie(request);

        // 2. Fallback: request body (for mobile)
        if (rawToken == null) {
            try {
                var body = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(request.getInputStream());
                if (body.has("refreshToken")) {
                    rawToken = body.get("refreshToken").asText();
                }
            } catch (Exception ignored) {}
        }

        AuthResponse auth = authService.refresh(rawToken, response);
        return ResponseEntity.ok(ApiResponse.ok(auth, "Token refreshed successfully"));
    }

    // ── POST /api/v1/auth/logout — current session only ──────────
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("X-User-Id") UUID userId,
            @CookieValue(name = "refresh_token", required = false) String rawRefreshToken,
            HttpServletResponse response) {
        authService.logout(userId, rawRefreshToken, response);
        return ResponseEntity.ok(ApiResponse.ok(null, "Logged out from this device"));
    }

    // ── POST /api/v1/auth/logout-all — all sessions ───────────────
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @RequestHeader("X-User-Id") UUID userId,
            HttpServletResponse response) {
        authService.logoutAll(userId, response);
        return ResponseEntity.ok(ApiResponse.ok(null, "Logged out from all devices"));
    }

    // ── GET /api/v1/auth/me ────────────────────────────────────────────────
    // Protected — X-User-Id injected by gateway JWT filter
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(
            @RequestHeader("X-User-Id") UUID userId) {

        UserDTO user = authService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok(user, null));
    }

    // ── PUT /api/v1/auth/me ────────────────────────────────────────────────
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserDTO user = authService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(user, "Profile updated successfully"));
    }

    /**
     * POST /api/v1/auth/me/avatar
     * Accepts multipart/form-data with a file field named "avatar".
     * Converts to base64 data URL and stores in user.avatarUrl.
     * Max size enforced by Spring (configure in application.yml: spring.servlet.multipart.max-file-size=2MB)
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserDTO>> uploadAvatar(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam("avatar") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NOT_FOUND","No file provided"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NOT_ALLOWED","Only image files are allowed"));
        }
        // 2 MB limit
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NOT_ALLOWED","Image must be under 2 MB"));
        }
        UserDTO updated = authService.updateAvatar(userId, file);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Avatar updated"));
    }

    // ── DELETE /api/v1/auth/me ─────────────────────────────────────────────
    // Soft-deletes account (GDPR compliance)
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestHeader("X-User-Id") UUID userId,
            HttpServletResponse response) {

        authService.deleteAccount(userId, response);
        return ResponseEntity.ok(ApiResponse.ok(null, "Account deleted successfully"));
    }

    // ── GET /api/v1/auth/health ────────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("auth-service:UP", null));
    }

    // ── POST /api/v1/auth/forgot-password ─────────────────────────────────
// Public — always returns 200 (don't reveal if email exists)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody com.corecompass.auth.dto.PasswordResetDtos.ForgotPasswordRequest request) {

        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null,
                "If an account exists for this email, an OTP has been sent"));
    }

    // ── POST /api/v1/auth/verify-otp ──────────────────────────────────────
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<com.corecompass.auth.dto.PasswordResetDtos.VerifyOtpResponse>> verifyOtp(
            @Valid @RequestBody com.corecompass.auth.dto.PasswordResetDtos.VerifyOtpRequest request) {

        var result = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(result, result.getMessage()));
    }

    // ── POST /api/v1/auth/reset-password ──────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody com.corecompass.auth.dto.PasswordResetDtos.ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null,
                "Password updated successfully. Please log in with your new password"));
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────────────────

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
            .filter(c -> "refreshToken".equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}
