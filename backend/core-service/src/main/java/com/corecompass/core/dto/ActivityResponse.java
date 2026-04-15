package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    private UUID id;
    private UUID goalId;
    private String activityTypeName;
    private String activityTypeIcon;
    private String note;
    private java.math.BigDecimal value;
    private String unit;
    private Instant loggedAt;
}
