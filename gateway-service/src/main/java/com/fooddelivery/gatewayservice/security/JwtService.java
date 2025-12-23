package com.fooddelivery.gatewayservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service // Marks this class as a Spring service
public class JwtService {

    // Secret key used to sign and verify JWT tokens
    // Loaded from application.yml
    @Value("${jwt.secret}")
    private String secret;

    // Converts the secret string into a cryptographic signing key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Validates the JWT token and returns its claims
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                // Use the signing key to verify token integrity
                .setSigningKey(getSigningKey())

                // Allow small clock differences between systems (60 seconds)
                .setAllowedClockSkewSeconds(60)

                // Build the JWT parser
                .build()

                // Parse the token and validate signature + expiration
                .parseClaimsJws(token)

                // Return the payload (claims) if token is valid
                .getBody();
    }
}