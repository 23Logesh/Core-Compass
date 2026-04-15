package com.corecompass.fitness.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExerciseSetRequest {
    @NotBlank
    @Size(max = 100)
    private String exerciseName;
    @NotNull
    @Min(1)
    private Integer setNumber;
    @Min(0)
    private Integer reps;
    @DecimalMin("0.0")
    private BigDecimal weightKg;
    private Integer durationSeconds;
}
