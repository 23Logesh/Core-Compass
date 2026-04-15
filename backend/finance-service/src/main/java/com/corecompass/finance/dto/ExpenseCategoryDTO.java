package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

// TYPE DTOs
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategoryDTO {
    private UUID id;
    private String name;
    private String icon;
    private String color;
    private boolean isSystem;
}
