package com.corecompass.report.dto;

import lombok.*;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDTO {
    private String month;               // e.g. "2026-04"
    private int    weeksCovered;        // how many weekly reports were aggregated
    private double avgGoalProgress;
    private int    totalWorkouts;
    private double totalCaloriesBurned;
    private double avgSleepHours;
    private double totalNetSavings;
    private int    avgHabitScore;
    private List<String> topInsights;  // collected from all weeks, deduped
}