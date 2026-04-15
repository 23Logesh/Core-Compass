package com.corecompass.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

// BUDGET
@Data
public class BudgetRequest {
    @NotNull
    private UUID categoryId;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal budgetAmount;
}
