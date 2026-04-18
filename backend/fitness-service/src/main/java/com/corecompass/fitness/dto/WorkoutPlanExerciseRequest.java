package com.corecompass.fitness.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WorkoutPlanExerciseRequest {

    @NotBlank
    @Size(max = 100)
    private String exerciseName;

    // Optional — links this entry to the exercise library
    private UUID exerciseId;

    @NotNull
    @Min(1) @Max(7)
    private Integer dayNumber;

    @Min(1)
    private Integer sets;

    @Min(1)
    private Integer targetReps;

    @DecimalMin("0.0")
    private BigDecimal targetWeightKg;

    @Size(max = 200)
    private String notes;

    @Min(0)
    private Integer sortOrder;
}