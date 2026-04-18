package com.corecompass.fitness.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FoodRequest {

    @NotBlank @Size(max = 120)
    private String name;

    @Size(max = 100)
    private String brand;

    @NotNull @DecimalMin("0.0")
    private BigDecimal caloriesPer100g;

    @DecimalMin("0.0") private BigDecimal proteinPer100g;
    @DecimalMin("0.0") private BigDecimal carbsPer100g;
    @DecimalMin("0.0") private BigDecimal fatPer100g;

    @DecimalMin("0.0") private BigDecimal servingSizeG;

    @Pattern(regexp = "SOLID|LIQUID", message = "foodType must be SOLID or LIQUID")
    private String foodType;
}