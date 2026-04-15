package com.corecompass.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    schema = "auth_schema",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_google_id", columnList = "google_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;   // null for OAuth-only users

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;       // null for email/password users

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String role = "USER";  // USER | ADMIN

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
