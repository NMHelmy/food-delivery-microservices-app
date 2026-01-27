package com.fooddelivery.authservice.controller;

import com.fooddelivery.authservice.dto.*;
import com.fooddelivery.authservice.model.User;
import com.fooddelivery.authservice.security.JwtService;
import com.fooddelivery.authservice.service.UserService;
import com.fooddelivery.authservice.dto.ForgotPasswordRequestDTO;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fooddelivery.authservice.exception.BadRequestException;
import com.fooddelivery.authservice.exception.UnauthorizedException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    // PUBLIC

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        String token = jwtService.generateToken(Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "fullName", user.getFullName()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new AuthResponse(
                        token,
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name(),
                        "User registered successfully"
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userService.login(request.getEmail(), request.getPassword());

        String token = jwtService.generateToken(Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "fullName", user.getFullName()
        ));

        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name(),
                        "Login successful"
                )
        );
    }

    // INTERNAL

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(
            @RequestBody Map<String, String> request) {

        String token = request.get("token");

        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Token is required");
        }

        try {
            Claims claims = jwtService.validateToken(token);

            return ResponseEntity.ok(
                    new ValidationResponse(
                            true,
                            claims.get("userId", Long.class),
                            claims.get("email", String.class),
                            claims.get("role", String.class)
                    )
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    // PUBLIC

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody ForgotPasswordRequestDTO request) {

        String resetToken = userService.generatePasswordResetToken(request.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "Password reset token generated",
                "resetToken", resetToken
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> request) {

        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");

        userService.resetPassword(resetToken, newPassword);

        return ResponseEntity.ok(
                Map.of("message", "Password reset successful")
        );
    }

    // AUTHENTICATED USERS

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        userService.changePassword(userId, oldPassword, newPassword);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserInfo(
            @RequestHeader("X-User-Id") Long userId) {

        User user = userService.getUserById(userId);

        return ResponseEntity.ok(
                Map.of(
                        "userId", user.getId(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "phoneNumber", user.getPhoneNumber(),
                        "role", user.getRole().name(),
                        "isActive", user.isActive(),
                        "isEmailVerified", user.isEmailVerified()
                )
        );
    }

    // ADMIN - Internal
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserInfo(
            @PathVariable Long userId) {

        User user = userService.getUserById(userId);

        return ResponseEntity.ok(
                Map.of(
                        "userId", user.getId(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "phoneNumber", user.getPhoneNumber(),
                        "role", user.getRole().name(),
                        "isActive", user.isActive(),
                        "isEmailVerified", user.isEmailVerified()
                )
        );
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {

        List<User> users = userService.getAllUsers();

        List<Map<String, Object>> response = users.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getId());
                    map.put("email", user.getEmail());
                    map.put("fullName", user.getFullName());
                    map.put("phoneNumber", user.getPhoneNumber());
                    map.put("role", user.getRole().name());
                    map.put("isActive", user.isActive());
                    map.put("isEmailVerified", user.isEmailVerified());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}
