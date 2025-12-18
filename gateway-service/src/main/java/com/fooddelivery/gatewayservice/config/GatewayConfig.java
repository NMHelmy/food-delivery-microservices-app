package com.fooddelivery.gatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth_route", r -> r.path("/auth/**")
                        .uri("lb://auth-service"))
                .route("user_route", r -> r.path("/api/users/**")
                        .uri("lb://user-service"))
                .route("restaurant_route", r -> r.path("/api/restaurants/**")
                        .uri("lb://restaurant-service"))
                .route("order_route", r -> r.path("/api/orders/**")
                        .uri("lb://order-service"))
                .route("delivery_route", r -> r.path("/deliveries/**")
                        .uri("lb://delivery-service"))

                .build();
    }
}