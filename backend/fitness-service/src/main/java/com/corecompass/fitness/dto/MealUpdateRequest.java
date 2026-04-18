package com.corecompass.fitness.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MealUpdateRequest {
    @Pattern(regexp = "BREAKFAST|LUNCH|DINNER|SNACK") private String mealType;
    private LocalDate mealDate;
    private LocalTime mealTime;
    @DecimalMin("0.0") private BigDecimal totalCalories;
    @DecimalMin("0.0") private BigDecimal proteinG;
    @DecimalMin("0.0") private BigDecimal carbsG;
    @DecimalMin("0.0") private BigDecimal fatG;
    @Size(max = 200) private String mealName;
    private String notes;
}