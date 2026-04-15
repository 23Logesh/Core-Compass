package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "workout_sessions", schema = "fitness_schema",
        indexes = @Index(name = "idx_workout_user_date", columnList = "user_id,session_date"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WorkoutSessionEntity {
    @Id @UuidGenerator @Column(updatable = false) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    // Matches V3 SQL: workout_name column
    @Column(name = "workout_name", nullable = false, length = 120) private String workoutName;
    // Matches V3 SQL: session_date column
    @Column(name = "session_date", nullable = false) private LocalDate sessionDate;
    @Column(name = "duration_minutes") private Integer durationMinutes;
    @Column(name = "total_volume_kg", precision = 10, scale = 2) private BigDecimal totalVolumeKg;
    private String notes;
    @Column(name = "is_deleted") @Builder.Default private boolean isDeleted = false;
    @Column(name = "created_at", updatable = false) @Builder.Default private Instant createdAt = Instant.now();
    @Column(name = "updated_at") @Builder.Default private Instant updatedAt = Instant.now();
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}