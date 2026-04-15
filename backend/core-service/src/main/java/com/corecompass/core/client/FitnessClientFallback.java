package com.corecompass.core.client;

import com.corecompass.core.dto.FitnessSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
class FitnessClientFallback implements FitnessClient {
    @Override
    public FitnessSummaryDTO getWeeklySummary(UUID userId, String weekStart) {
        log.warn("fitness-service unavailable — returning empty summary for userId={}", userId);
        return FitnessSummaryDTO.builder()
                .workoutsThisWeek(0)
                .caloriesBurnedThisWeek(0)
                .avgSleepHours(0)
                .currentStreak(0)
                .build();
    }
}
