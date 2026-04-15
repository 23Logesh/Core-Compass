package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

// Table name MUST match V3 SQL: exercise_sets (not exercise_logs)
@Entity
@Table(name = "exercise_sets", schema = "fitness_schema")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WorkoutExerciseEntity {
    @Id @UuidGenerator @Column(updatable = false) private UUID id;
    @Column(name = "session_id", nullable = false) private UUID sessionId;
    @Column(name = "exercise_name", nullable = false, length = 100) private String exerciseName;
    @Column(name = "set_number", nullable = false) private Integer setNumber;
    private Integer reps;
    @Column(name = "weight_kg", precision = 6, scale = 2) private BigDecimal weightKg;
    @Column(name = "duration_seconds") private Integer durationSeconds;
}