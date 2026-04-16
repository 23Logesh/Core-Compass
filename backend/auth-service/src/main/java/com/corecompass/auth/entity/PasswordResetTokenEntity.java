package com.corecompass.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "password_reset_tokens",
        schema = "auth_schema"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "otp_hash", nullable = false, length = 64)
    private String otpHash;               // SHA-256 of 6-digit OTP

    @Column(name = "reset_token_hash", length = 64)
    private String resetTokenHash;        // SHA-256 of UUID — set after OTP verified

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;     // true after OTP successfully checked

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;         // true after password actually changed

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;             // wrong OTP attempt counter (max 3)

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}