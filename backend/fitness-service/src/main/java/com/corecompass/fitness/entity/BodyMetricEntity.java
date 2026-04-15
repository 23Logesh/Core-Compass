package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// BODY METRICS  (FIX: added 'unit' and 'notes' fields)
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "body_metrics", schema = "fitness_schema",
        indexes = @Index(name = "idx_body_metrics_user_date", columnList = "user_id,logged_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyMetricEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    // WEIGHT_KG | BODY_FAT_PCT | CHEST_CM | WAIST_CM | HIPS_CM | BICEP_CM
    @Column(name = "metric_type", nullable = false, length = 30)
    private String metricType;
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal value;
    // FIX: added unit field (e.g. "kg", "cm", "%")
    @Column(nullable = false, length = 20)
    private String unit;
    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;
    // FIX: added notes field
    private String notes;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
