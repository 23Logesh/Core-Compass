package com.corecompass.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// MILESTONE — sub-targets within a Goal
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "milestones", schema = "core_schema",
       indexes = @Index(name = "idx_milestones_goal_id", columnList = "goal_id"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MilestoneEntity {

    @Id @UuidGenerator @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
