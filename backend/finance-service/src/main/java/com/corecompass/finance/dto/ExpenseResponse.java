package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private UUID id;
    private BigDecimal amount;
    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private UUID paymentMethodId;
    private String paymentMethodName;
    private LocalDate expenseDate;
    private String merchant;
    private String note;
    private List<String> tags;
    private boolean isRecurring;
    private Instant createdAt;
}
