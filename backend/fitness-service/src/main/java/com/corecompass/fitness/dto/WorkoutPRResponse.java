package com.corecompass.fitness.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class WorkoutPRResponse {
    private String exerciseName;
    private BigDecimal maxWeightKg;
    private Integer maxReps;
    private BigDecimal maxVolumeKg;
    private LocalDate achievedOn;
}