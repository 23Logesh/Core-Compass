package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// FEIGN INTERNAL DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSummaryDTO {
    private double monthlyIncome;
    private double monthlyExpenses;
    private double netSavings;
    private int healthScore;
}
