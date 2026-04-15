package com.corecompass.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j circuit breaker defaults for all downstream service calls.
 *
 * Circuit opens after 5 consecutive failures.
 * Half-open: allows 3 calls to test recovery.
 * Timeout per call: 5 seconds (matches Feign read timeout in other services).
 */
@Configuration
public class CircuitBreakerConfig2 {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCircuitBreakerCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .timeLimiterConfig(
                TimeLimiterConfig.custom()
                    .timeoutDuration(Duration.ofSeconds(5))
                    .build()
            )
            .circuitBreakerConfig(
                CircuitBreakerConfig.custom()
                    .slidingWindowSize(10)
                    .failureRateThreshold(50)           // Open after 50% failures
                    .waitDurationInOpenState(Duration.ofSeconds(15))
                    .permittedNumberOfCallsInHalfOpenState(3)
                    .slowCallDurationThreshold(Duration.ofSeconds(3))
                    .slowCallRateThreshold(80)
                    .build()
            )
            .build()
        );
    }
}
