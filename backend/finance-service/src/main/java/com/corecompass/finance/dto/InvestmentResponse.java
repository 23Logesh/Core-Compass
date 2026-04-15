package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentResponse {
    private UUID id;
    private String investmentTypeName;
    private String name;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private Double returnsPercent;
    private LocalDate purchaseDate;
    private Instant createdAt;
}
