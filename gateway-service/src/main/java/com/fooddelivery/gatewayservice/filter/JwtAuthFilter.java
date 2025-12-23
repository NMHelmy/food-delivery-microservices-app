package com.fooddelivery.gatewayservice.filter;

import com.fooddelivery.gatewayservice.security.JwtService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component // Registers this filter as a Spring bean
@Order(-100) // Ensures this filter runs before Spring Security authorization
public class JwtAuthFilter implements WebFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthFilter.class);

    // Service responsible for validating JWT tokens
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Determines whether the request path is public and does not require JWT
    private boolean isPublicPath(String path) {
        return path.equals("/auth/login")
                || path.equals("/auth/register")
                || path.equals("/auth/forgot-password")
                || path.equals("/auth/reset-password")
                || (path.startsWith("/restaurants") && !path.contains("/owner"));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        log.info("JWT FILTER → {} {}", exchange.getRequest().getMethod(), path);

        // Skip JWT validation for public endpoints
        if (isPublicPath(path)) {
            log.info("Public path → skipping JWT");
            return chain.filter(exchange);
        }

        // Read Authorization header
        String authHeader =
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        log.info("Authorization header = {}", authHeader);

        // Reject request if Authorization header is missing or malformed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            // Extract token value (remove "Bearer ")
            String token = authHeader.substring(7);

            // Validate token and extract claims
            Claims claims = jwtService.validateToken(token);

            log.info("JWT validated → claims={}", claims);

            // Extract required claims from token
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            // Reject token if required claims are missing
            if (userId == null || role == null) {
                log.warn("JWT missing required claims");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Forward user identity to downstream microservices via headers
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Role", role)
                    .build();

            // Create Spring Security authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, // principal
                            null,   // no password needed
                            List.of(new SimpleGrantedAuthority(role)) // user role
                    );

            log.info("Authentication injected into SecurityContext");

            // Continue filter chain and inject authentication into security context
            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .contextWrite(
                            ReactiveSecurityContextHolder.withAuthentication(authentication)
                    );

        } catch (Exception e) {
            // Token is invalid, expired, or tampered with
            log.error("JWT validation failed", e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
