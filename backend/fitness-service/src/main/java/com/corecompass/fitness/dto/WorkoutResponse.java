package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutResponse {
    private UUID id;
    private String workoutName;
    private LocalDate sessionDate;
    private Integer durationMinutes;
    private BigDecimal totalVolumeKg;
    private String notes;
    private List<ExerciseSetResponse> exerciseSets;
    private Instant createdAt;
}
