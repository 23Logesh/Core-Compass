package com.corecompass.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

// Habit score from habits-service
@FeignClient(name = "habits-service", path = "/internal/habits", fallback = HabitsClientFallback.class)
public interface HabitsClient {
    @GetMapping("/score")
    Integer getHabitScore(@RequestParam UUID userId);
}
