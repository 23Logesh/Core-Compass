package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Feign-fetched from fitness-service
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitnessSummaryDTO {
    private int workoutsThisWeek;
    private double caloriesBurnedThisWeek;
    private double avgSleepHours;
    private int currentStreak;
}
