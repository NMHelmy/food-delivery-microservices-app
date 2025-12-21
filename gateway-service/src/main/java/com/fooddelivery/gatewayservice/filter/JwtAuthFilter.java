package com.fooddelivery.gatewayservice.filter;

import com.fooddelivery.gatewayservice.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements WebFilter {

    @Autowired
    private JwtService jwtService;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/",           // All auth endpoints (login, register, refresh)
            "/actuator/",       // Health checks and metrics
            "/eureka/"          // Eureka endpoints
    );

    // Specific public GET endpoints for restaurants
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/restaurants" // Only the list endpoint (will check exact match or with query params)
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // Check if path is public (auth, actuator, eureka)
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Allow public GET access ONLY to specific endpoints
        // GET /api/restaurants (list all) and GET /api/restaurants/{id} (get by id)
        if ("GET".equals(method) && isPublicGetPath(path)) {
            return chain.filter(exchange);
        }

        // All other paths require authentication
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            // Validate token and extract claims
            Claims claims = jwtService.validateToken(token);

            // Extract user information
            String userId = String.valueOf(claims.get("userId"));
            String role = String.valueOf(claims.get("role"));
            String email = claims.getSubject();

            // Create modified request with headers
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .header("X-User-Email", email)
                    .build();

            // Create modified exchange with the new request
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            // Continue with modified exchange (WITH headers!)
            return chain.filter(modifiedExchange);

        } catch (JwtException e) {
            // Invalid or expired token
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isPublicGetPath(String path) {
        // Allow GET /api/restaurants (exact match or with query params)
        if (path.equals("/api/restaurants")) {
            return true;
        }

        // Allow GET /api/restaurants/{id} where {id} is a number
        if (path.matches("/api/restaurants/\\d+")) {
            return true;
        }

        return false;
    }
}