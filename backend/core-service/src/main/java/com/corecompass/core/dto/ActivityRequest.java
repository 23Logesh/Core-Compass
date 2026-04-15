package com.corecompass.core.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ActivityRequest {
    @NotNull(message = "activityTypeId is required")
    private UUID activityTypeId;
    @Size(max = 500)
    private String note;
    private java.math.BigDecimal value;
    private String unit;
}
