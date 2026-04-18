package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** GET /api/v1/finance/cash-flow?months=6 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CashFlowResponse {

    private List<MonthlyFlow> months;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netCashFlow;      // totalIncome - totalExpenses
    private BigDecimal avgMonthlyIncome;
    private BigDecimal avgMonthlyExpenses;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyFlow {
        private String     month;        // "2025-04"
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;          // income - expenses
        private String     status;       // SURPLUS | DEFICIT | BREAK_EVEN
    }
}