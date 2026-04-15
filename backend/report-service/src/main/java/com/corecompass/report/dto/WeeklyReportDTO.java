package com.corecompass.report.dto;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportDTO {
    private UUID id;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private int activeGoals;
    private double avgGoalProgress;
    private int todosCompleted;
    private int workoutsCount;
    private double caloriesBurned;
    private double avgSleepHours;
    private double netSavings;
    private int habitScore;
    private List<String> insights;
    private Instant createdAt;
}

