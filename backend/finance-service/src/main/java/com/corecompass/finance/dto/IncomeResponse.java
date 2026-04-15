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
public class IncomeResponse {
    private UUID id;
    private BigDecimal amount;
    private String sourceType;
    private String note;
    private LocalDate incomeDate;
    private boolean isRecurring;
    private Instant createdAt;
}
