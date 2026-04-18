package com.corecompass.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void createNotification(UUID userId, String type, String title, String message) {
        // Silent fallback — report was already saved, notification delivery
        // is best-effort. Core-service being down must not affect report generation.
        log.warn("NotificationClient fallback — could not deliver notification " +
                "for userId={} type={}", userId, type);
    }
}