package com.corecompass.fitness.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class MonthlySummaryDTO {
    private int year;
    private int month;
    private int totalWorkouts;
    private int totalCardioSessions;
    private int totalCaloriesBurned;
    private BigDecimal totalVolumeKg;
    private BigDecimal avgSleepHours;
    private BigDecimal avgMoodScore;
    private int totalHydrationMl;
    private BigDecimal avgDailyCalories;
    private int workoutStreakEnd;
}