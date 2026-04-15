package com.corecompass.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDTO {
    private UUID id;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private int habitScore;
    private double avgGoalProgress;
    private int workoutsCount;
    private Instant createdAt;
}
