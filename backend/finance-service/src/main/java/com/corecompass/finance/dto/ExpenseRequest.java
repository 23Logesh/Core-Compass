package com.corecompass.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// EXPENSE
@Data
public class ExpenseRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotNull
    private UUID categoryId;
    private UUID subCategoryId;
    private UUID paymentMethodId;
    private LocalDate date;
    @Size(max = 100)
    private String merchant;
    @Size(max = 200)
    private String note;
    private List<String> tags;
    private boolean isRecurring;
}
