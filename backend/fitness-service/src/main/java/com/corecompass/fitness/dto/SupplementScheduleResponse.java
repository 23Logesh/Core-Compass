package com.corecompass.fitness.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SupplementScheduleResponse {
    private UUID       id;
    private UUID       supplementTypeId;
    private String     supplementTypeName;
    private BigDecimal doseAmount;
    private String     doseUnit;
    private String     timing;
    private String     frequency;
    private boolean    isActive;
    private Instant    createdAt;
}