package com.corecompass.fitness.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExerciseResponse {
    private UUID    id;
    private String  name;
    private String  muscleGroup;
    private String  equipment;
    private String  difficulty;
    private String  instructions;
    private String  videoUrl;
    private boolean isSystem;
    private Instant createdAt;
}