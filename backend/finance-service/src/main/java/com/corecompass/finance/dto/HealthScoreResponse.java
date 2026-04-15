package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// HEALTH SCORE (4 components per API doc)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthScoreResponse {
    private int score;
    private String grade;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal netSavings;
    private BigDecimal totalDebt;
    private double savingsRatePct;
    private double debtToIncomePct;
}
