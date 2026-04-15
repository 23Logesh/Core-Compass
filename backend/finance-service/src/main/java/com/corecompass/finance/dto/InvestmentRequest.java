package com.corecompass.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// INVESTMENTS
@Data
public class InvestmentRequest {
    @NotNull
    private UUID investmentTypeId;
    @NotBlank
    @Size(max = 120)
    private String name;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    @NotNull
    private LocalDate purchaseDate;
    private LocalDate maturityDate;
    private String notes;
}
