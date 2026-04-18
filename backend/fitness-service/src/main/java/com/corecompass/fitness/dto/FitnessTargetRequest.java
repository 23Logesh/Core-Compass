package com.corecompass.fitness.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FitnessTargetRequest {
    private Integer weeklyWorkoutTarget;
    private BigDecimal dailyCalorieTarget;
    private BigDecimal dailyProteinTargetG;
    private Integer dailyHydrationTargetMl;
    private BigDecimal dailyCalorieBurnTarget;
}