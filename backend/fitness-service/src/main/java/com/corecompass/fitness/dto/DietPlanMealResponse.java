package com.corecompass.fitness.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DietPlanMealResponse {
    private UUID       id;
    private int        dayNumber;
    private String     mealType;
    private UUID       foodId;
    private String     foodName;
    private BigDecimal quantityG;
    private BigDecimal calories;
    private BigDecimal proteinG;
    private BigDecimal carbsG;
    private BigDecimal fatG;
    private int        sortOrder;
}