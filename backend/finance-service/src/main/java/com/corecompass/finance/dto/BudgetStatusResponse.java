package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatusResponse {
    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private String monthYear;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double percentageUsed;
    private boolean exceeded;
}
