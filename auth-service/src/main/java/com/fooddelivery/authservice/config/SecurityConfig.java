//package com.fooddelivery.authservice.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//
//                        // PUBLIC AUTH ENDPOINTS
//                        .requestMatchers("/auth/**").permitAll()
//
//                        // TRUST GATEWAY FOR ADDRESSES
//                        .requestMatchers("/addresses/**").permitAll()
//
//                        // INTERNAL / SERVICE-TO-SERVICE (if any)
//                        .requestMatchers("/internal/**").permitAll()
//
//                        // EVERYTHING ELSE (OPTIONAL)
//                        .anyRequest().permitAll()
//                );
//
//        return http.build();
//    }
//}
