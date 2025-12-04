package com.gamestore.gatewayservice.config;

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
                .route("catalog_route", r -> r.path("/catalog/**")
                        .uri("lb://catalog-service"))
                .route("cart_route", r -> r.path("/cart/**")
                        .uri("lb://cart-service"))
                .route("order_route", r -> r.path("/order/**")
                        .uri("lb://order-service"))
                .route("test_route", r -> r.path("/test/**")
                        .uri("lb://test-service"))
                .build();
    }
}
