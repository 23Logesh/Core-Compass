package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSetResponse {
    private UUID id;
    private String exerciseName;
    private int setNumber;
    private Integer reps;
    private BigDecimal weightKg;
    private Integer durationSeconds;
}
