package com.fooddelivery.authservice.controller;

import com.fooddelivery.authservice.dto.*;
import com.fooddelivery.authservice.model.User;
import com.fooddelivery.authservice.security.JwtService;
import com.fooddelivery.authservice.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request);

            String token = jwtService.generateToken(Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole().name(),
                    "fullName", user.getFullName()
            ));

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().name(),
                    "User registered successfully"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());

            String token = jwtService.generateToken(Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole().name(),
                    "fullName", user.getFullName()
            ));

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().name(),
                    "Login successful"
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Token validation endpoint - other services will call this
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ValidationResponse(false, "Token is required"));
            }

            Claims claims = jwtService.validateToken(token);

            ValidationResponse response = new ValidationResponse(
                    true,
                    claims.get("userId", Long.class),
                    claims.get("email", String.class),
                    claims.get("role", String.class)
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidationResponse(false, "Invalid or expired token"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String resetToken = userService.generatePasswordResetToken(email);

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset token generated",
                    "resetToken", resetToken
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");

            userService.resetPassword(resetToken, newPassword);

            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            Long userId = Long.parseLong(request.get("userId"));
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            userService.changePassword(userId, oldPassword, newPassword);

            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);

            return ResponseEntity.ok(Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "phoneNumber", user.getPhoneNumber(),
                    "role", user.getRole().name(),
                    "isActive", user.isActive(),
                    "isEmailVerified", user.isEmailVerified()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            // Validate token and check if user is ADMIN
            Claims claims = jwtService.validateToken(token.replace("Bearer ", ""));
            String role = claims.get("role", String.class);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admin only."));
            }

            List<User> users = userService.getAllUsers();

            List<Map<String, Object>> userResponses = users.stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", user.getId());
                        userMap.put("email", user.getEmail());
                        userMap.put("fullName", user.getFullName());
                        userMap.put("phoneNumber", user.getPhoneNumber());
                        userMap.put("role", user.getRole().name());
                        userMap.put("isActive", user.isActive());
                        userMap.put("isEmailVerified", user.isEmailVerified());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userResponses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}