package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetHistoryResponse {
    private String     month;           // "YYYY-MM"
    private BigDecimal totalBudgeted;   // sum of all budget limits that month
    private BigDecimal totalSpent;      // sum of all expenses that month
    private BigDecimal surplus;         // totalBudgeted - totalSpent (negative = overspent)
    private double     adherencePct;    // (totalSpent / totalBudgeted) * 100, capped at 200
}