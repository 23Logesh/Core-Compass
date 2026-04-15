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
public class InvestmentSummaryResponse {
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private double returnsPercent;
    private int count;
}
