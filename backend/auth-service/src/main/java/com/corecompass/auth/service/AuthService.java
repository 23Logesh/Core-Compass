package com.corecompass.auth.service;

import com.corecompass.auth.dto.*;
import com.corecompass.auth.entity.RefreshTokenEntity;
import com.corecompass.auth.entity.UserEntity;
import com.corecompass.auth.exception.*;
import com.corecompass.auth.repository.PasswordResetTokenRepository;
import com.corecompass.auth.repository.RefreshTokenRepository;
import com.corecompass.auth.repository.UserRepository;
import com.corecompass.auth.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

import java.util.UUID;

import com.corecompass.auth.dto.PasswordResetDtos.*;
import com.corecompass.auth.entity.PasswordResetTokenEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService            jwtService;
    private final PasswordEncoder       passwordEncoder;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    @Value("${jwt.refresh-token-expiry-seconds:604800}")   // 7 days
    private long refreshTokenExpirySeconds;

    // ──────────────────────────────────────────────────────────
    // REGISTER
    // ──────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("EMAIL_ALREADY_EXISTS",
                "An account with this email already exists");
        }

        // Create user
        UserEntity user = UserEntity.builder()
            .email(request.getEmail().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .name(request.getName().trim())
            .role("USER")
            .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user, response);
    }

    // ──────────────────────────────────────────────────────────
    // LOGIN
    // ──────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        UserEntity user = userRepository
            .findByEmailAndIsDeletedFalse(request.getEmail().toLowerCase())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new AccountDisabledException("This account has been disabled");
        }

        if (user.getPasswordHash() == null) {
            throw new InvalidCredentialsException(
                "This account uses Google Sign-In. Please login with Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, response);
    }

    // ──────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ──────────────────────────────────────────────────────────
    // ADD THIS (the new method):
    @Transactional
    public AuthResponse refresh(String rawRefreshToken, HttpServletResponse response) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token is missing");
        }

        // SHA-256 hash is searchable (unlike BCrypt) — direct DB lookup, no full-table scan
        String tokenHash = hashWithSha256(rawRefreshToken);

        RefreshTokenEntity storedToken = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid or expired"));

        // Rotate — revoke old, issue new
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        UserEntity user = userRepository.findByIdAndIsDeletedFalse(storedToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        if (!user.isActive()) {
            throw new AccountDisabledException("This account has been disabled");
        }

        log.info("Token refreshed for userId: {}", user.getId());
        return buildAuthResponse(user, response);
    }

    // ──────────────────────────────────────────────────────────
    // LOGOUT
    // ──────────────────────────────────────────────────────────

    // ── Logout current session only ───────────────────────────────
    @Transactional
    public void logout(UUID userId, String rawRefreshToken, HttpServletResponse response) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            // Revoke only the token that was sent with this request
            String tokenHash = hashWithSha256(rawRefreshToken);
            refreshTokenRepository.revokeByTokenHash(tokenHash);
            log.info("Single session revoked for userId={}", userId);
        } else {
            // Fallback — if no token in cookie (e.g. mobile header flow), revoke all
            refreshTokenRepository.revokeAllByUserId(userId);
            log.info("No token found — all sessions revoked as fallback for userId={}", userId);
        }
        clearRefreshTokenCookie(response);
    }

    // ── Logout all sessions ───────────────────────────────────────
    @Transactional
    public void logoutAll(UUID userId, HttpServletResponse response) {
        refreshTokenRepository.revokeAllByUserId(userId);
        clearRefreshTokenCookie(response);
        log.info("All sessions revoked for userId={}", userId);
    }

    // ──────────────────────────────────────────────────────────
    // GOOGLE OAUTH2 — called after Spring Security OAuth2 callback
    // ──────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse loginWithGoogle(String googleId, String email,
                                        String name, String avatarUrl,
                                        HttpServletResponse response) {
        // Find existing user by googleId or email, or create new
        UserEntity user = userRepository.findByGoogleIdAndIsDeletedFalse(googleId)
            .or(() -> userRepository.findByEmailAndIsDeletedFalse(email))
            .map(existing -> {
                // Link Google account if not already linked
                if (existing.getGoogleId() == null) {
                    existing.setGoogleId(googleId);
                }
                if (avatarUrl != null && existing.getAvatarUrl() == null) {
                    existing.setAvatarUrl(avatarUrl);
                }
                return userRepository.save(existing);
            })
            .orElseGet(() -> {
                // New user via Google
                UserEntity newUser = UserEntity.builder()
                    .email(email.toLowerCase())
                    .googleId(googleId)
                    .name(name)
                    .avatarUrl(avatarUrl)
                    .role("USER")
                    .build();
                return userRepository.save(newUser);
            });

        log.info("Google login for: {}", user.getEmail());
        return buildAuthResponse(user, response);
    }

    // ──────────────────────────────────────────────────────────
    // GET PROFILE
    // ──────────────────────────────────────────────────────────

    public UserDTO getProfile(UUID userId) {
        UserEntity user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toUserDTO(user);
    }

    // ──────────────────────────────────────────────────────────
    // UPDATE PROFILE
    // ──────────────────────────────────────────────────────────

    @Transactional
    public UserDTO updateProfile(UUID userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getName() != null) user.setName(request.getName().trim());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        return toUserDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO updateAvatar(UUID userId, org.springframework.web.multipart.MultipartFile file) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        try {
            byte[] bytes = file.getBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String dataUrl = "data:" + file.getContentType() + ";base64," + base64;
            user.setAvatarUrl(dataUrl);
            userRepository.save(user);
            log.info("Avatar updated for userId={}", userId);
            return toUserDTO(user);
        } catch (java.io.IOException e) {
            log.error("Avatar upload failed for userId={}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to process avatar upload");
        }
    }

    // ──────────────────────────────────────────────────────────
    // SOFT DELETE (GDPR)
    // ──────────────────────────────────────────────────────────

    @Transactional
    public void deleteAccount(UUID userId, HttpServletResponse response) {
        UserEntity user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(userId);
        clearRefreshTokenCookie(response);
        log.info("Account soft-deleted: {}", userId);
    }

    // ──────────────────────────────────────────────────────────
    // CLEANUP CRON — runs daily at 02:00 IST
    // ──────────────────────────────────────────────────────────

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant cutoff = Instant.now();
        refreshTokenRepository.deleteExpiredAndRevoked(cutoff);
        resetTokenRepository.deleteExpiredOrUsed(cutoff);
        log.info("Cleanup: expired/revoked refresh tokens + password reset tokens deleted");
    }

    // ──────────────────────────────────────────────────────────
// FORGOT PASSWORD — Step 1: send OTP
// ──────────────────────────────────────────────────────────

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        // Always respond the same — don't reveal if email exists (security)
        userRepository.findByEmailAndIsDeletedFalse(email).ifPresent(user -> {
            // Invalidate any existing pending tokens for this email
            resetTokenRepository
                    .findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(email, Instant.now())
                    .ifPresent(existing -> {
                        existing.setUsed(true);
                        resetTokenRepository.save(existing);
                    });

            // Generate 6-digit OTP
            String otp     = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            String otpHash = hashWithSha256(otp);

            PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                    .email(email)
                    .otpHash(otpHash)
                    .expiresAt(Instant.now().plusSeconds(900)) // 15 minutes
                    .build();

            resetTokenRepository.save(token);

            // Send OTP email (async — won't block response)
            emailService.sendOtpEmail(email, user.getName(), otp);
            log.info("Password reset OTP sent to: {}", email);
        });
    }

// ──────────────────────────────────────────────────────────
// VERIFY OTP — Step 2: validate OTP, return reset token
// ──────────────────────────────────────────────────────────

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        PasswordResetTokenEntity token = resetTokenRepository
                .findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(email, Instant.now())
                .orElseThrow(() -> new InvalidTokenException("OTP is invalid or has expired"));

        if (token.getAttempts() >= 3) {
            token.setUsed(true);
            resetTokenRepository.save(token);
            throw new InvalidTokenException("Too many failed attempts. Please request a new OTP");
        }

        if (!token.getOtpHash().equals(hashWithSha256(request.getOtp()))) {
            token.setAttempts(token.getAttempts() + 1);
            resetTokenRepository.save(token);
            int remaining = 3 - token.getAttempts();
            throw new InvalidTokenException("Incorrect OTP. " + remaining + " attempt(s) remaining");
        }

        // OTP correct — generate a short-lived reset token
        String rawResetToken  = UUID.randomUUID().toString();
        String resetTokenHash = hashWithSha256(rawResetToken);

        token.setVerified(true);
        token.setResetTokenHash(resetTokenHash);
        // Reset token expires in 10 minutes from now
        token.setExpiresAt(Instant.now().plusSeconds(600));
        resetTokenRepository.save(token);

        log.info("OTP verified for: {}", email);
        return new VerifyOtpResponse(rawResetToken, "OTP verified. Use resetToken to set your new password");
    }

// ──────────────────────────────────────────────────────────
// RESET PASSWORD — Step 3: set new password
// ──────────────────────────────────────────────────────────

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashWithSha256(request.getResetToken());

        PasswordResetTokenEntity token = resetTokenRepository
                .findByResetTokenHashAndVerifiedTrueAndUsedFalseAndExpiresAtAfter(tokenHash, Instant.now())
                .orElseThrow(() -> new InvalidTokenException("Reset token is invalid or has expired"));

        UserEntity user = userRepository
                .findByEmailAndIsDeletedFalse(token.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new AccountDisabledException("This account has been disabled");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token used
        token.setUsed(true);
        resetTokenRepository.save(token);

        // Revoke ALL refresh tokens — force re-login on all devices
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Password reset successful for: {}", token.getEmail());
    }

    // ──────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ──────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(UserEntity user, HttpServletResponse response) {
        // Generate access token (JWT)
        String accessToken = jwtService.generateAccessToken(
            user.getId(), user.getEmail(), user.getRole()
        );

        // Generate refresh token (raw UUID → BCrypt hash stored in DB)
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = hashWithSha256(rawRefreshToken);
        Instant expiresAt      = Instant.now().plusSeconds(refreshTokenExpirySeconds);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
            .userId(user.getId())
            .tokenHash(tokenHash)
            .expiresAt(expiresAt)
            .build();
        refreshTokenRepository.save(refreshToken);

        // Set refresh token as HttpOnly cookie
        setRefreshTokenCookie(response, rawRefreshToken);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .expiresIn(jwtService.getAccessTokenExpirySeconds())
            .user(toUserDTO(user))
            .build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie("refreshToken", rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);          // HTTPS only in production
        cookie.setPath("/api/v1/auth");  // Only sent to /auth/* routes
        cookie.setMaxAge((int) refreshTokenExpirySeconds);
        // SameSite=Strict via header (Cookie API doesn't support it directly in older servlets)
        response.addHeader("Set-Cookie",
            String.format("refreshToken=%s; Path=/api/v1/auth; HttpOnly; Secure; SameSite=Strict; Max-Age=%d",
                rawToken, refreshTokenExpirySeconds));
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
            "refreshToken=; Path=/api/v1/auth; HttpOnly; Secure; SameSite=Strict; Max-Age=0");
    }

    private UserDTO toUserDTO(UserEntity user) {
        return UserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole())
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .build();
    }

    private String hashWithSha256(String raw) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
