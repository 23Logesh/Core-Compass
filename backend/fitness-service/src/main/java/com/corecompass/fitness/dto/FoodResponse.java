package com.corecompass.fitness.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FoodResponse {
    private UUID       id;
    private String     name;
    private String     brand;
    private BigDecimal caloriesPer100g;
    private BigDecimal proteinPer100g;
    private BigDecimal carbsPer100g;
    private BigDecimal fatPer100g;
    private BigDecimal servingSizeG;
    private String     foodType;
    private boolean    isSystem;
    private Instant    createdAt;
}