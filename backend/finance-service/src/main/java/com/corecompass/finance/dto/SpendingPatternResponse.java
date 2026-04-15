package com.corecompass.finance.dto;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

// SPENDING ANALYTICS
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class SpendingPatternResponse {
    private BigDecimal weekdayAvg; private BigDecimal weekendAvg; private String peakSpendingDay;
    private List<MerchantSpend> topMerchants; private List<MonthlyTrend> monthlyTrend;
    @Data @AllArgsConstructor @NoArgsConstructor public static class MerchantSpend { private String merchant; private BigDecimal total; }
    @Data @AllArgsConstructor @NoArgsConstructor public static class MonthlyTrend  { private String month; private BigDecimal total; }
}

