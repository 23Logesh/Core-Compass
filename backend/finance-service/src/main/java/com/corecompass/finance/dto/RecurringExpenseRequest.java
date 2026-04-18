package com.corecompass.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class RecurringExpenseRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private UUID categoryId;

    private UUID subCategoryId;

    private UUID paymentMethodId;

    @Size(max = 100)
    private String merchant;

    @Size(max = 200)
    private String note;

    @NotNull
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|YEARLY",
            message = "frequency must be DAILY, WEEKLY, MONTHLY, or YEARLY")
    private String frequency;

    // Day of month (1-31) for MONTHLY; day of week (1-7 Mon=1) for WEEKLY
    private Integer dayOfPeriod;

    private LocalDate startsOn;
    private LocalDate endsOn;
}