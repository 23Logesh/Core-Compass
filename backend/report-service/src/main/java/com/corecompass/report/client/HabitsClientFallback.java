package com.corecompass.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
class HabitsClientFallback implements HabitsClient {
    public Integer getHabitScore(UUID u) {
        return 0;
    }
}
