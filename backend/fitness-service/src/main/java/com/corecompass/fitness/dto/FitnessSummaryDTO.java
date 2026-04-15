package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitnessSummaryDTO {
    private int workoutsThisWeek; private double caloriesBurnedThisWeek;
    private double avgSleepHours; private int currentStreak;
}
