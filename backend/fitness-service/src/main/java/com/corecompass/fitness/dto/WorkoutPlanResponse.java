package com.corecompass.fitness.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WorkoutPlanResponse {
    private UUID       id;
    private String     name;
    private String     description;
    private String     difficulty;
    private int        daysPerWeek;
    private boolean    isActive;
    private List<WorkoutPlanExerciseResponse> exercises;
    private Instant    createdAt;
    private Instant    updatedAt;
}