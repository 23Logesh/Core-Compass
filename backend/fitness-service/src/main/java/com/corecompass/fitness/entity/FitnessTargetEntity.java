package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fitness_targets", schema = "fitness_schema")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FitnessTargetEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "weekly_workout_target")
    private Integer weeklyWorkoutTarget;

    @Column(name = "daily_calorie_target", precision = 8, scale = 2)
    private BigDecimal dailyCalorieTarget;

    @Column(name = "daily_protein_target_g", precision = 6, scale = 2)
    private BigDecimal dailyProteinTargetG;

    @Column(name = "daily_hydration_target_ml")
    private Integer dailyHydrationTargetMl;

    @Column(name = "daily_calorie_burn_target", precision = 8, scale = 2)
    private BigDecimal dailyCalorieBurnTarget;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}