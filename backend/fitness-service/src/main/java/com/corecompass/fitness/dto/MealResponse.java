package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealResponse {
    private UUID id;
    private String mealType;
    private LocalDate mealDate;
    private LocalTime mealTime;
    private BigDecimal totalCalories;
    private BigDecimal proteinG;
    private BigDecimal carbsG;
    private BigDecimal fatG;
    private String mealName;
    private Instant createdAt;
}
