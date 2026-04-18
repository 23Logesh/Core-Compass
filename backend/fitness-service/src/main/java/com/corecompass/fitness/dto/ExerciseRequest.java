package com.corecompass.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExerciseRequest {

    @NotBlank
    @Size(max = 80)
    private String name;

    @NotBlank
    @Pattern(regexp = "CHEST|BACK|SHOULDERS|BICEPS|TRICEPS|LEGS|GLUTES|CORE|FULL_BODY|CARDIO|OTHER",
            message = "Invalid muscle group")
    private String muscleGroup;

    @Pattern(regexp = "BARBELL|DUMBBELL|MACHINE|CABLE|BODYWEIGHT|RESISTANCE_BAND|KETTLEBELL|NONE|CUSTOM",
            message = "Invalid equipment")
    private String equipment;

    @Pattern(regexp = "BEGINNER|INTERMEDIATE|ADVANCED",
            message = "difficulty must be BEGINNER, INTERMEDIATE, or ADVANCED")
    private String difficulty;

    private String instructions;

    @Size(max = 500)
    private String videoUrl;
}