package com.corecompass.fitness.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DietPlanResponse {
    private UUID       id;
    private String     name;
    private String     description;
    private String     goal;
    private BigDecimal dailyCalorieTarget;
    private BigDecimal dailyProteinG;
    private BigDecimal dailyCarbsG;
    private BigDecimal dailyFatG;
    private boolean    isActive;
    private List<DietPlanMealResponse> meals;
    private Instant    createdAt;
    private Instant    updatedAt;
}