package com.corecompass.auth.service;

import com.corecompass.auth.dto.*;
import com.corecompass.auth.entity.RefreshTokenEntity;
import com.corecompass.auth.entity.UserEntity;
import com.corecompass.auth.exception.*;
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

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService            jwtService;
    private final PasswordEncoder       passwordEncoder;

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

    @Transactional
    public void logout(UUID userId, HttpServletResponse response) {
        refreshTokenRepository.revokeAllByUserId(userId);
        clearRefreshTokenCookie(response);
        log.info("User logged out, all tokens revoked: {}", userId);
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
        log.info("Cleanup: expired/revoked refresh tokens deleted");
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
