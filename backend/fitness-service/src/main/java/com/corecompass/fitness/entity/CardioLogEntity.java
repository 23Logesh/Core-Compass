package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// CARDIO LOG
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "cardio_logs", schema = "fitness_schema",
        indexes = @Index(name = "idx_cardio_user_date", columnList = "user_id,logged_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardioLogEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    // FIX: renamed from 'type' → 'cardioType' to match service/repo usage
    @Column(name = "cardio_type", nullable = false, length = 60)
    private String cardioType;
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;
    @Column(precision = 6, scale = 2)
    private BigDecimal distanceKm;
    @Column(name = "calories_burned")
    private Integer caloriesBurned;
    @Column(name = "avg_heart_rate")
    private Integer avgHeartRate;
    private String notes;
    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;
    // FIX: added isDeleted so repository soft-delete queries work
    @Column(name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
