package com.corecompass.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

// DEBT
@Data
public class DebtRequest {
    @NotBlank
    @Size(max = 120)
    private String name;
    private String debtType;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal principalAmount;
    private BigDecimal currentBalance;
    private BigDecimal interestRate;
    private BigDecimal minPayment;
}
