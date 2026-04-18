package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workout_plans", schema = "fitness_schema",
        indexes = @Index(name = "idx_workout_plans_user", columnList = "user_id,is_active"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WorkoutPlanEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String difficulty = "BEGINNER";

    @Column(name = "days_per_week", nullable = false)
    @Builder.Default
    private int daysPerWeek = 3;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;

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
    void onUpdate() { this.updatedAt = Instant.now(); }
}