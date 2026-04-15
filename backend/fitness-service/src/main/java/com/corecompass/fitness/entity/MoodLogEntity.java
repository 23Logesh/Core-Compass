package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// MOOD LOG
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "mood_logs", schema = "fitness_schema",
        indexes = @Index(name = "idx_mood_user_date", columnList = "user_id,logged_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodLogEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    // GREAT | GOOD | NEUTRAL | TIRED | SICK
    @Column(nullable = false, length = 20)
    private String mood;
    @Column(name = "energy_level")
    private Integer energyLevel; // 1-10
    private String notes;
    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
