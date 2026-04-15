package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtResponse {
    private UUID id;
    private String name;
    private String debtType;
    private BigDecimal principalAmount;
    private BigDecimal currentBalance;
    private BigDecimal interestRate;
    private BigDecimal minPayment;
    private Instant createdAt;
}
