package com.corecompass.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "finance-notification-client",
        url = "${services.core-service.url:http://core-service:8082}",
        fallback = NotificationClientFallback.class
)
public interface NotificationClient {

    @PostMapping("/internal/core/notifications")
    void createNotification(
            @RequestParam UUID userId,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam String message
    );
}