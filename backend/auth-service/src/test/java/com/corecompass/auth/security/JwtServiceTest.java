package com.corecompass.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    JwtService jwtService;

    static final String TEST_SECRET =
        "CoreCompassSuperSecretKeyForJWTTokenGenerationMinimum256BitsLong";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirySeconds", 900L);
    }

    @Test
    @DisplayName("generateAccessToken: produces a non-blank token")
    void generateToken_notBlank() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "user@test.com", "USER");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("extractUserId: returns correct userId from valid token")
    void extractUserId_validToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "user@test.com", "USER");
        UUID extracted = jwtService.extractUserId(token);
        assertThat(extracted).isEqualTo(userId);
    }

    @Test
    @DisplayName("isTokenValid: returns true for fresh token")
    void isTokenValid_freshToken_true() {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "x@y.com", "USER");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: returns false for garbage string")
    void isTokenValid_garbage_false() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("parseClaims: email claim matches what was passed")
    void parseClaims_emailClaim() {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "check@email.com", "USER");
        var claims = jwtService.parseClaims(token);
        assertThat(claims.get("email", String.class)).isEqualTo("check@email.com");
    }

    @Test
    @DisplayName("parseClaims: role claim matches what was passed")
    void parseClaims_roleClaim() {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "a@b.com", "ADMIN");
        var claims = jwtService.parseClaims(token);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }
}
