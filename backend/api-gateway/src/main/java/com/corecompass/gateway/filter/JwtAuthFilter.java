package com.corecompass.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Global JWT authentication filter.
 *
 * Flow:
 *  1. Check if path is public → skip validation
 *  2. Extract Bearer token from Authorization header
 *  3. Validate JWT signature + expiry using shared secret
 *  4. Inject X-User-Id and X-User-Role headers for downstream services
 *  5. Return 401 on any auth failure
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Paths that bypass JWT validation.
     * These must match the api-gateway route prefixes exactly.
     */
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/refresh",
        "/api/v1/auth/oauth2",
        "/actuator/health",
        "/actuator/info"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 1. Public paths — pass through immediately
        if (isPublicPath(path)) {
            log.debug("Public path, skipping JWT check: {}", path);
            return chain.filter(exchange);
        }

        // 2. Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header on path: {}", path);
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED", "Missing or invalid Authorization header");
        }

        // 3. Validate JWT
        String token = authHeader.substring(7);
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String userId = claims.getSubject();
            String role   = claims.get("role", String.class);
            String email  = claims.get("email", String.class);

            if (userId == null || userId.isBlank()) {
                return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN", "Token subject (userId) is missing");
            }

            // 4. Inject headers for downstream microservices
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id",    userId)
                .header("X-User-Role",  role  != null ? role  : "USER")
                .header("X-User-Email", email != null ? email : "")
                .build();

            log.debug("JWT validated. userId={} role={} path={}", userId, role, path);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT on path {}: {}", path, ex.getMessage());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                "TOKEN_EXPIRED", "Access token has expired. Please refresh.");

        } catch (SignatureException ex) {
            log.warn("Invalid JWT signature on path {}: {}", path, ex.getMessage());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                "INVALID_SIGNATURE", "Token signature verification failed");

        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT on path {}: {}", path, ex.getMessage());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                "MALFORMED_TOKEN", "Token format is invalid");

        } catch (JwtException ex) {
            log.warn("JWT error on path {}: {}", path, ex.getMessage());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                "INVALID_TOKEN", "Token validation failed");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Builds a standardized JSON error response matching the CoreCompass API envelope format.
     */
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange,
                                          HttpStatus status,
                                          String errorCode,
                                          String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Matches the API standard error envelope defined in the LLD
        String body = String.format("""
            {
              "success": false,
              "error": {
                "code": "%s",
                "message": "%s",
                "field": null
              },
              "timestamp": "%s"
            }""",
            errorCode, message, Instant.now().toString()
        );

        DataBuffer buffer = response.bufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Run before all other filters (highest priority).
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
