package com.corecompass.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Handles JWT access token generation and validation.
 *
 * Access token:  HS256, 15-minute TTL, stored in JS memory (Zustand)
 * Refresh token: Raw UUID, BCrypt-hashed in DB, stored in HttpOnly cookie
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry-seconds:900}")   // 15 min default
    private long accessTokenExpirySeconds;

    /**
     * Generate a signed JWT access token.
     *
     * @param userId UUID of the authenticated user
     * @param email  User email (embedded in token for convenience)
     * @param role   User role: USER | ADMIN
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        Instant now    = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpirySeconds);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .claim("type", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Extract userId (subject) from a valid token.
     * Returns null if token is invalid or expired.
     */
    public UUID extractUserId(String token) {
        try {
            String subject = parseClaims(token).getSubject();
            return UUID.fromString(subject);
        } catch (Exception e) {
            log.warn("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract all claims. Throws JwtException on invalid/expired tokens.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpirySeconds;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
