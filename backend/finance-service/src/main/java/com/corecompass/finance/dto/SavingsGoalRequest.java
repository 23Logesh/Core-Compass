package com.corecompass.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// SAVINGS GOALS
@Data
public class SavingsGoalRequest {
    @NotBlank
    @Size(max = 120)
    private String title;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal targetAmount;
    private LocalDate targetDate;
}
