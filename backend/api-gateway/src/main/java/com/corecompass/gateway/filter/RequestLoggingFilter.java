package com.corecompass.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Logs every request/response pair through the gateway.
 * Captures: method, path, userId (from injected header), status, duration.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        String method = request.getMethod().name();
        String path   = request.getPath().value();
        String userId = request.getHeaders().getFirst("X-User-Id");

        log.info("→ {} {} [userId={}]", method, path, userId != null ? userId : "anonymous");

        return chain.filter(exchange).doFinally(signalType -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = response.getStatusCode() != null
                ? response.getStatusCode().value() : 0;

            if (statusCode >= 500) {
                log.error("← {} {} [{}] {}ms | ERROR", method, path, statusCode, duration);
            } else if (statusCode >= 400) {
                log.warn("← {} {} [{}] {}ms", method, path, statusCode, duration);
            } else {
                log.info("← {} {} [{}] {}ms", method, path, statusCode, duration);
            }
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // Just after JWT filter
    }
}
