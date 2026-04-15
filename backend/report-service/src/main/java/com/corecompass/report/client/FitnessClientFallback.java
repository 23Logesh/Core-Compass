package com.corecompass.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
class FitnessClientFallback implements FitnessClient {
    public FitnessSummaryDTO getWeeklySummary(UUID u, String w) {
        return new FitnessSummaryDTO(0, 0, 0, 0);
    }
}
