package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMacroSummary {
    private LocalDate date;
    private BigDecimal totalCalories;
    private BigDecimal totalProteinG;
    private BigDecimal totalCarbsG;
    private BigDecimal totalFatG;
    private List<MealResponse> meals;
}
