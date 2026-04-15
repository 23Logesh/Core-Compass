package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// NET WORTH
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetWorthResponse {
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;
    private String month;
}
