package com.corecompass.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

// Fitness summary from fitness-service
@FeignClient(name = "fitness-service", path = "/internal/fitness", fallback = FitnessClientFallback.class)
public interface FitnessClient {
    @GetMapping("/summary/weekly")
    FitnessSummaryDTO getWeeklySummary(@RequestParam UUID userId, @RequestParam String weekStart);
}
