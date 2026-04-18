package com.corecompass.finance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RecurringExpenseResponse {
    private UUID       id;
    private BigDecimal amount;
    private UUID       categoryId;
    private String     categoryName;
    private String     categoryIcon;
    private UUID       paymentMethodId;
    private String     paymentMethodName;
    private String     merchant;
    private String     note;
    private String     frequency;
    private Integer    dayOfPeriod;
    private LocalDate  startsOn;
    private LocalDate  endsOn;
    private boolean    isActive;
    private Instant    createdAt;
}