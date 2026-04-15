package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Feign-fetched from finance-service
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSummaryDTO {
    private double monthlyIncome;
    private double monthlyExpenses;
    private double netSavings;
    private int healthScore;   // 0-100
}
