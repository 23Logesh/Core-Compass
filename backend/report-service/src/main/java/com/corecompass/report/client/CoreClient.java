package com.corecompass.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

// Goal progress from core-service
@FeignClient(name = "core-service", path = "/internal/core", fallback = CoreClientFallback.class)
public interface CoreClient {
    @GetMapping("/active-users")
    List<UUID> getActiveUserIds();

    @GetMapping("/goal-progress")
    GoalProgressDTO getGoalProgress(@RequestParam UUID userId);
}
