package com.corecompass.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
class CoreClientFallback implements CoreClient {
    public List<UUID> getActiveUserIds() {
        log.warn("core-service unavailable");
        return List.of();
    }

    public GoalProgressDTO getGoalProgress(UUID u) {
        return new GoalProgressDTO(0, 0, 0);
    }
}
