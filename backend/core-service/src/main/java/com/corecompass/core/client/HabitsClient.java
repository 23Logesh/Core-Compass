package com.corecompass.core.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// HABITS SERVICE FEIGN CLIENT
// ─────────────────────────────────────────────────────────────
@FeignClient(
        name = "habits-service",
        path = "/internal/habits",
        fallback = HabitsClientFallback.class
)
public interface HabitsClient {

    @GetMapping("/score")
    Integer getHabitScore(@RequestParam UUID userId);
}
