package com.corecompass.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback responses when a downstream service's circuit breaker opens.
 * Returns a structured 503 response so the frontend can handle gracefully.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback(ServerWebExchange exchange) {
        return fallback("auth-service", exchange);
    }

    @GetMapping("/core")
    public Mono<ResponseEntity<Map<String, Object>>> coreFallback(ServerWebExchange exchange) {
        return fallback("core-service", exchange);
    }

    @GetMapping("/fitness")
    public Mono<ResponseEntity<Map<String, Object>>> fitnessFallback(ServerWebExchange exchange) {
        return fallback("fitness-service", exchange);
    }

    @GetMapping("/finance")
    public Mono<ResponseEntity<Map<String, Object>>> financeFallback(ServerWebExchange exchange) {
        return fallback("finance-service", exchange);
    }

    @GetMapping("/habits")
    public Mono<ResponseEntity<Map<String, Object>>> habitsFallback(ServerWebExchange exchange) {
        return fallback("habits-service", exchange);
    }

    @GetMapping("/report")
    public Mono<ResponseEntity<Map<String, Object>>> reportFallback(ServerWebExchange exchange) {
        return fallback("report-service", exchange);
    }

    private Mono<ResponseEntity<Map<String, Object>>> fallback(String service, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        log.error("Circuit breaker OPEN for {}. Incoming path: {}", service, path);

        Map<String, Object> body = Map.of(
            "success",   false,
            "error",     Map.of(
                "code",    "SERVICE_UNAVAILABLE",
                "message", service + " is temporarily unavailable. Please try again shortly.",
                "field",   null
            ),
            "timestamp", Instant.now().toString()
        );

        return Mono.just(
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
        );
    }
}
