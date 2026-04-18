package com.corecompass.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class ExpenseCategoryRequest {
    @NotBlank(message = "name is required")
    @Size(max = 60)
    private String name;

    @Size(max = 10)
    private String icon;

    @Size(max = 7, message = "color must be a hex code e.g. #FF5733")
    private String color;

    // optional — if provided, creates a sub-category under this parent
    private UUID parentId;
}