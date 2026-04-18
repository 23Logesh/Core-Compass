package com.corecompass.fitness.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class FitnessTargetResponse {
    private UUID id;
    private Integer weeklyWorkoutTarget;
    private BigDecimal dailyCalorieTarget;
    private BigDecimal dailyProteinTargetG;
    private Integer dailyHydrationTargetMl;
    private BigDecimal dailyCalorieBurnTarget;
    private Instant updatedAt;
}