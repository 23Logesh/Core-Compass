package com.corecompass.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Rate limiting key resolvers.
 *
 * - ipKeyResolver    → used for auth endpoints (10 req/min per IP = brute-force protection)
 * - userKeyResolver  → used for authenticated endpoints (100 req/min per userId)
 *
 * Note: In production, plug in Redis for distributed rate limiting.
 * For zero-cost local dev, Spring Cloud Gateway uses in-memory by default.
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Rate limit by remote IP address.
     * Used for: /auth/login, /auth/register
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String ip = remoteAddress != null
                ? remoteAddress.getAddress().getHostAddress()
                : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * Rate limit by authenticated user ID (injected by JwtAuthFilter).
     * Falls back to IP if header not present (shouldn't happen post-auth).
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }
            // Fallback to IP
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String ip = remoteAddress != null
                ? remoteAddress.getAddress().getHostAddress()
                : "unknown";
            return Mono.just("ip:" + ip);
        };
    }
}
