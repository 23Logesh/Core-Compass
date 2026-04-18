package com.corecompass.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingsGoalUpdateRequest {
    @Size(max = 120)
    private String title;

    @DecimalMin("0.01")
    private BigDecimal targetAmount;

    private LocalDate targetDate;
}