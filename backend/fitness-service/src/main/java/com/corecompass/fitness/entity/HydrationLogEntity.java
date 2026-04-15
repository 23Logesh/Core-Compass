package com.corecompass.fitness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// HYDRATION LOG
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "hydration_logs", schema = "fitness_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HydrationLogEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "amount_ml", nullable = false)
    private Integer amountMl;
    @Column(name = "target_ml")
    private Integer targetMl;
    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
