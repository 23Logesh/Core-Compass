package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// SLEEP LOG  (FIX: aligned field names with service usage)
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "sleep_logs", schema = "fitness_schema",
        indexes = @Index(name = "idx_sleep_user_date", columnList = "user_id,sleep_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepLogEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "sleep_date", nullable = false)
    private LocalDate sleepDate;
    // FIX: renamed bedTime → bedtime & wakeTime stays wakeTime (Lombok generates getBedtime/getWakeTime)
    @Column(name = "bedtime")
    private LocalTime bedtime;
    @Column(name = "wake_time")
    private LocalTime wakeTime;
    @Column(name = "duration_hours", precision = 4, scale = 2)
    private BigDecimal durationHours;
    @Column(name = "quality_rating")
    private Integer qualityRating; // 1-5
    private String notes;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
