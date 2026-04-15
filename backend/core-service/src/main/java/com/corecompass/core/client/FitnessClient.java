package com.corecompass.core.client;

import com.corecompass.core.dto.FitnessSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// FITNESS SERVICE FEIGN CLIENT
// ─────────────────────────────────────────────────────────────
@FeignClient(
        name = "fitness-service",
        path = "/internal/fitness",
        fallback = FitnessClientFallback.class
)
public interface FitnessClient {

    @GetMapping("/summary/weekly")
    FitnessSummaryDTO getWeeklySummary(
            @RequestParam UUID userId,
            @RequestParam String weekStart
    );
}
