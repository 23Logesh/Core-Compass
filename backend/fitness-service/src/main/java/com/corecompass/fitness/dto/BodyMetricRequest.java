package com.corecompass.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BodyMetricRequest {
    @NotBlank
    private String metricType; @NotNull
    private BigDecimal value;
    @NotBlank private String unit; private LocalDate loggedDate; private String notes;
}
