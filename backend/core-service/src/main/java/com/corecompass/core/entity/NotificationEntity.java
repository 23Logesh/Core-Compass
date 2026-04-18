package com.corecompass.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "core_schema",
        indexes = {
                @Index(name = "idx_notifications_user",    columnList = "user_id,is_read"),
                @Index(name = "idx_notifications_created", columnList = "user_id,created_at")
        })
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // GOAL_DUE | HABIT_REMINDER | BUDGET_ALERT | WEEKLY_REPORT | GENERAL
    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    // Optional extra data (e.g. goalId, habitId) stored as JSON string
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}