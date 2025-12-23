package com.fooddelivery.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration // Marks this class as a Spring configuration class
@EnableWebFluxSecurity // Enables Spring Security for reactive (WebFlux) applications
public class GatewayConfig {

    // Defines the security filter chain used by the API Gateway
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        http
                // Disable CSRF because this is a stateless REST API using JWT
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Do not store security context in sessions (JWT = stateless)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // Start defining authorization rules
                .authorizeExchange(exchanges -> exchanges

                        // PUBLIC
                        // No authentication required
                        .pathMatchers(HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/forgot-password",
                                "/auth/reset-password"
                        ).permitAll()

                        // Public restaurant browsing
                        .pathMatchers(HttpMethod.GET,
                                "/restaurants",
                                "/restaurants/*",
                                "/restaurants/*/menu",
                                "/restaurants/*/menu/*"
                        ).permitAll()

                        // INTERNAL
                        // Block all external access completely
                        .pathMatchers(HttpMethod.POST, "/auth/validate").denyAll()
                        .pathMatchers(HttpMethod.PATCH, "/restaurants/*/rating").denyAll()
                        .pathMatchers(HttpMethod.POST, "/orders/*/paid").denyAll()

                        // ADMIN only
                        .pathMatchers(
                                "/auth/users",
                                "/auth/user/*",
                                "/addresses/user/*",
                                "/restaurants/owner/*",
                                "/orders",
                                "/orders/customer/*",
                                "/orders/status/*",
                                "/orders/*/payment",
                                "/deliveries/admin/**",
                                "/deliveries/customer/*",
                                "/deliveries/*",
                                "/notifications/send"
                        ).hasAuthority("ADMIN")

                        // Admin payment control
                        .pathMatchers(HttpMethod.GET,
                                "/payments",
                                "/payments/customer/*"
                        ).hasAuthority("ADMIN")

                        .pathMatchers(HttpMethod.POST,
                                "/payments/*/refund"
                        ).hasAuthority("ADMIN")


                        // DELIVERY DRIVER
                        .pathMatchers(
                                "/drivers/my-profile",
                                "/deliveries/my-driver-deliveries",
                                "/deliveries/driver/active",
                                "/deliveries/*/pickup-confirmation",
                                "/deliveries/*/delivery-confirmation"
                        ).hasAuthority("DELIVERY_DRIVER")

                        // RESTAURANT OWNER
                        .pathMatchers(
                                "/restaurants/owner",
                                "/restaurants",
                                "/restaurants/*",
                                "/restaurants/*/menu/**",
                                "/deliveries/my-restaurant-deliveries"
                        ).hasAuthority("RESTAURANT_OWNER")

                        // CUSTOMER
                        .pathMatchers(HttpMethod.POST, "/orders").hasAuthority("CUSTOMER")

                        .pathMatchers(
                                "/orders/customer",
                                "/deliveries/my-deliveries"
                        ).hasAuthority("CUSTOMER")

                        .pathMatchers(HttpMethod.POST,
                                "/payments",
                                "/payments/*/confirm",
                                "/payments/*/cancel"
                        ).hasAuthority("CUSTOMER")

                        .pathMatchers(HttpMethod.GET,
                                "/payments/customer"
                        ).hasAuthority("CUSTOMER")

                        // MULTI-ROLE
                        .pathMatchers(HttpMethod.GET, "/orders/*")
                        .hasAnyAuthority("ADMIN", "CUSTOMER")

                        .pathMatchers("/orders/restaurant/*")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.PUT, "/orders/*/status")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.POST, "/deliveries")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers("/deliveries/my-order/*")
                        .hasAnyAuthority("ADMIN", "CUSTOMER")

                        .pathMatchers(HttpMethod.PUT, "/deliveries/*/status")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers("/deliveries/*/assign-driver")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers("/drivers/*/status")
                        .hasAnyAuthority("ADMIN", "DELIVERY_DRIVER")

                        .pathMatchers("/drivers/available")
                        .hasAnyAuthority("ADMIN")

                        .pathMatchers(HttpMethod.GET,
                                "/payments/*",
                                "/payments/order/*"
                        ).hasAnyAuthority("ADMIN", "CUSTOMER")

                        // AUTHENTICATED (ALL)
                        .pathMatchers(
                                "/auth/me",
                                "/auth/change-password",
                                "/addresses/**",
                                "/notifications/**"
                        ).authenticated()

                        // FALLBACK
                        // Any endpoint not explicitly defined above requires authentication
                        .anyExchange().authenticated()
                );

        // Build and return the security configuration
        return http.build();
    }
}
