package com.corecompass.fitness.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DietPlanMealRequest {

    @NotNull @Min(1) @Max(7)
    private Integer dayNumber;

    @NotBlank
    @Pattern(regexp = "BREAKFAST|LUNCH|DINNER|SNACK")
    private String mealType;

    // Optional — links to food library for auto-calculation
    private UUID foodId;

    @NotBlank @Size(max = 120)
    private String foodName;

    @DecimalMin("0.0")
    private BigDecimal quantityG;

    @Min(0) private Integer sortOrder;
}