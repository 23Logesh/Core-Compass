package com.corecompass.fitness.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SupplementLogResponse {
    private UUID       id;
    private UUID       supplementTypeId;
    private String     supplementTypeName;
    private String     supplementTypeCategory;
    private BigDecimal doseAmount;
    private String     doseUnit;
    private String     timing;
    private LocalDate  loggedDate;
    private String     notes;
    private Instant    createdAt;
}