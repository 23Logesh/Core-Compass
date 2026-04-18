package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/** One distinct income source the user has used, with total for context. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeSourceResponse {
    private String     sourceType;   // e.g. "Salary", "Freelance"
    private long       timesLogged;
    private BigDecimal totalAmount;
}