package com.corecompass.fitness.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class WorkoutPlanRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 300)
    private String description;

    @Pattern(regexp = "BEGINNER|INTERMEDIATE|ADVANCED",
            message = "difficulty must be BEGINNER, INTERMEDIATE, or ADVANCED")
    private String difficulty;

    @Min(1) @Max(7)
    private Integer daysPerWeek;

    @Valid
    private List<WorkoutPlanExerciseRequest> exercises;
}