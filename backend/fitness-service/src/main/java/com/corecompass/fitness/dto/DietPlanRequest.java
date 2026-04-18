package com.corecompass.fitness.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DietPlanRequest {

    @NotBlank @Size(max = 120)
    private String name;

    @Size(max = 300)
    private String description;

    @Pattern(regexp = "WEIGHT_LOSS|MUSCLE_GAIN|MAINTENANCE|CUSTOM",
            message = "goal must be WEIGHT_LOSS, MUSCLE_GAIN, MAINTENANCE, or CUSTOM")
    private String goal;

    @DecimalMin("0.0") private BigDecimal dailyCalorieTarget;
    @DecimalMin("0.0") private BigDecimal dailyProteinG;
    @DecimalMin("0.0") private BigDecimal dailyCarbsG;
    @DecimalMin("0.0") private BigDecimal dailyFatG;

    @Valid
    private List<DietPlanMealRequest> meals;
}