package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workout_plan_exercises", schema = "fitness_schema",
        indexes = @Index(name = "idx_plan_exercises_plan", columnList = "plan_id,day_number"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WorkoutPlanExerciseEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "day_number", nullable = false)
    @Builder.Default
    private int dayNumber = 1;

    @Column(name = "exercise_name", nullable = false, length = 100)
    private String exerciseName;

    // Optional FK to exercise library — null if user typed a custom name
    @Column(name = "exercise_id")
    private UUID exerciseId;

    @Column(nullable = false)
    @Builder.Default
    private int sets = 3;

    @Column(name = "target_reps")
    private Integer targetReps;

    @Column(name = "target_weight_kg", precision = 6, scale = 2)
    private BigDecimal targetWeightKg;

    @Column(length = 200)
    private String notes;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}