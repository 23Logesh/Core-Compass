package com.corecompass.fitness.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WorkoutPlanExerciseResponse {
    private UUID       id;
    private int        dayNumber;
    private String     exerciseName;
    private UUID       exerciseId;
    private int        sets;
    private Integer    targetReps;
    private BigDecimal targetWeightKg;
    private String     notes;
    private int        sortOrder;
}