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
public class BudgetAlertResponse {
    private UUID   categoryId;
    private String categoryName;
    private String categoryIcon;
    private String month;           // "YYYY-MM"
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private double percentageUsed;
    private boolean exceeded;       // true if spent >= budgetAmount
}