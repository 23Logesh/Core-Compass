package com.corecompass.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// ACTIVITY — a logged action tied to a goal
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "activities", schema = "core_schema",
       indexes = {
           @Index(name = "idx_activities_goal_id", columnList = "goal_id"),
           @Index(name = "idx_activities_user_id", columnList = "user_id")
       })
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityEntity {

    @Id @UuidGenerator @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "activity_type_id", nullable = false)
    private UUID activityTypeId;

    @Column(columnDefinition = "TEXT")
    private String note;

    // Optional numeric value (e.g., distance, reps, duration)
    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @Column(length = 20)
    private String unit;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "logged_at", nullable = false)
    @Builder.Default
    private Instant loggedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
