package com.corecompass.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// INCOME
@Data
public class IncomeRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotBlank
    private String sourceType;
    @Size(max = 200)
    private String note;
    private LocalDate date;
    private boolean isRecurring;
}
