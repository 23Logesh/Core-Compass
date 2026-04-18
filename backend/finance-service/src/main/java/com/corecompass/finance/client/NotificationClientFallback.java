package com.corecompass.finance.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void createNotification(UUID userId, String type, String title, String message) {
        // Best-effort — expense was already saved. Core-service being down
        // must never roll back a user's expense log.
        log.warn("NotificationClient fallback — budget alert not delivered for userId={}", userId);
    }
}