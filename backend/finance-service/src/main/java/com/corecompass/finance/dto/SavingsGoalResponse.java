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
public class SavingsGoalResponse {
    private UUID id;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private double progressPct;
    private Instant createdAt;
}
