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

                        // INTERNAL
                        // Block all external access completely
                        .pathMatchers(HttpMethod.POST, "/auth/validate").denyAll()
                        .pathMatchers(HttpMethod.PATCH, "/restaurants/*/rating").denyAll()
                        .pathMatchers(HttpMethod.POST, "/orders/*/paid").denyAll()
                        .pathMatchers(HttpMethod.POST, "/orders/from-cart").denyAll()
                        .pathMatchers(HttpMethod.GET, "/addresses/internal/**").denyAll()

                        // ============= DELIVERIES SECTION (REORDERED) =============
                        // CUSTOMER - specific paths first
                        .pathMatchers("/deliveries/my-deliveries")
                        .hasAuthority("CUSTOMER")

                        .pathMatchers("/deliveries/my-order/*")
                        .hasAnyAuthority("ADMIN", "CUSTOMER")

                        // DELIVERY_DRIVER - specific paths first
                        .pathMatchers("/deliveries/my-driver-deliveries")
                        .hasAuthority("DELIVERY_DRIVER")

                        .pathMatchers(
                                "/deliveries/driver/active",
                                "/deliveries/*/pickup-confirmation",
                                "/deliveries/*/delivery-confirmation"
                        ).hasAuthority("DELIVERY_DRIVER")

                        // RESTAURANT_OWNER - specific path first
                        .pathMatchers("/deliveries/my-restaurant-deliveries")
                        .hasAuthority("RESTAURANT_OWNER")

                        // ADMIN + RESTAURANT_OWNER - operations
                        .pathMatchers(HttpMethod.POST, "/deliveries")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.PUT, "/deliveries/*/status")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.PUT, "/deliveries/*/assign-driver")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        // ADMIN only - general patterns LAST
                        .pathMatchers(
                                "/deliveries/admin/**",
                                "/deliveries/customer/*"
                        ).hasAuthority("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/deliveries/*")
                        .hasAuthority("ADMIN")

                        // ============= NOTIFICATIONS SECTION =============

                        // ADMIN only - send notification endpoint
                        .pathMatchers(HttpMethod.POST, "/notifications/send")
                        .hasAuthority("ADMIN")

                        // AUTHENTICATED - all other notification endpoints
                        // (get notifications, mark as read, etc.)
                        .pathMatchers("/notifications/**")
                        .authenticated()


                        // ADMIN only
                        .pathMatchers(
                                "/auth/users",
                                "/auth/user/*",
                                "/addresses/user/*",
                                "/orders/customer/*",
                                "/orders/status/*",
                                "/orders/*/payment"
                        ).hasAuthority("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/orders")
                        .hasAuthority("ADMIN")

                        // Admin payment control
                        .pathMatchers(HttpMethod.GET,
                                "/payments",
                                "/payments/customer/*"
                        ).hasAuthority("ADMIN")

                        .pathMatchers(HttpMethod.POST,
                                "/payments/*/refund"
                        ).hasAuthority("ADMIN")

                        // RESTAURANT OWNER
                        .pathMatchers(HttpMethod.POST, "/restaurants")
                        .hasAuthority("RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.PUT, "/restaurants/*")
                        .hasAuthority("RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.PATCH, "/restaurants/*/activate", "/restaurants/*/deactivate")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        // RESTAURANT OWNER - manage their own menus (POST, PUT, DELETE only)
                        .pathMatchers(HttpMethod.POST, "/restaurants/*/menu/**")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.PUT, "/restaurants/*/menu/**")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.DELETE, "/restaurants/*/menu/**")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers(
                                "/restaurants/owner"
                        ).hasAuthority("RESTAURANT_OWNER")

                        // Public restaurant browsing
                        .pathMatchers(HttpMethod.GET,
                                "/restaurants",
                                "/restaurants/*",
                                "/restaurants/*/menu",
                                "/restaurants/*/menu/*"
                        ).permitAll()

                        // CUSTOMER
                        .pathMatchers(HttpMethod.POST, "/orders").hasAuthority("CUSTOMER")

                        .pathMatchers(
                                "/orders/customer"
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
                        // Drivers section - MOST SPECIFIC FIRST
                        .pathMatchers("/drivers/available")
                        .hasAuthority("ADMIN")

                        .pathMatchers("/drivers/*/status")
                        .hasAnyAuthority("ADMIN", "DELIVERY_DRIVER")

                        .pathMatchers(HttpMethod.GET, "/drivers/*")
                        .hasAuthority("ADMIN")

                        .pathMatchers(HttpMethod.PUT, "/drivers/my-profile")
                        .hasAuthority("DELIVERY_DRIVER")

                        // Orders section
                        .pathMatchers(HttpMethod.GET, "/orders/*")
                        .hasAnyAuthority("ADMIN", "CUSTOMER", "RESTAURANT_OWNER")

                        .pathMatchers("/orders/restaurant/*")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        .pathMatchers(HttpMethod.PUT, "/orders/*/status")
                        .hasAnyAuthority("ADMIN", "RESTAURANT_OWNER")

                        // Payments section
                        .pathMatchers(HttpMethod.GET,
                                "/payments/*",
                                "/payments/order/*"
                        ).hasAnyAuthority("ADMIN", "CUSTOMER")


                        //Customer-cart section
                        .pathMatchers(
                                "/cart",
                                "/cart/**"
                        ).hasAuthority("CUSTOMER")



                        // AUTHENTICATED (ALL)
                        .pathMatchers(
                                "/auth/me",
                                "/auth/change-password"
                        ).authenticated()

                        .pathMatchers(HttpMethod.GET, "/addresses/user/*")
                        .hasAuthority("ADMIN")

                        .pathMatchers("/addresses/**")
                        .hasAuthority("CUSTOMER")

                        // FALLBACK
                        // Any endpoint not explicitly defined above requires authentication
                        .anyExchange().authenticated()
                );

        // Build and return the security configuration
        return http.build();
    }
}
