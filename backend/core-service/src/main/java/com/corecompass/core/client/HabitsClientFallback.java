package com.corecompass.core.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
class HabitsClientFallback implements HabitsClient {
    @Override
    public Integer getHabitScore(UUID userId) {
        log.warn("habits-service unavailable for userId={}", userId);
        return 0;
    }
}
