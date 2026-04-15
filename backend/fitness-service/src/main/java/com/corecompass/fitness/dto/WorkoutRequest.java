package com.corecompass.fitness.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// ── WORKOUT ──────────────────────────────────────────────────────
@Data
public class WorkoutRequest {
    @NotBlank
    @Size(max = 120)
    private String workoutName;
    private LocalDate sessionDate;
    private Integer durationMinutes;
    private String notes;
    @Valid
    private List<ExerciseSetRequest> exerciseSets;
}
