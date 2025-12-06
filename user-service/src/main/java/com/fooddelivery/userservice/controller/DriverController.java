package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.DriverProfileDTO;
import com.fooddelivery.userservice.feign.AuthServiceClient;
import com.fooddelivery.userservice.model.DriverProfile;
import com.fooddelivery.userservice.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/driver")
public class DriverController {

    @Autowired
    private DriverService driverService;

    @Autowired
    private AuthServiceClient authServiceClient;

    private Long validateTokenAndGetUserId(String token) {
        Map<String, Object> response = authServiceClient.validateToken(Map.of("token", token));

        if (!(Boolean) response.get("valid")) {
            throw new RuntimeException("Invalid or expired token");
        }

        return ((Number) response.get("userId")).longValue();
    }

    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DriverProfileDTO dto) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = validateTokenAndGetUserId(token);

            dto.setUserId(userId);

            DriverProfile profile = driverService.createDriverProfile(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            DriverProfile profile = driverService.getDriverProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody DriverProfileDTO dto) {
        try {
            DriverProfile profile = driverService.updateDriverProfile(userId, dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            DriverProfile profile = driverService.updateDriverStatus(userId, status);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/location")
    public ResponseEntity<?> updateLocation(
            @PathVariable Long userId,
            @RequestBody Map<String, Double> request) {
        try {
            Double latitude = request.get("latitude");
            Double longitude = request.get("longitude");
            DriverProfile profile = driverService.updateDriverLocation(userId, latitude, longitude);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDrivers() {
        List<DriverProfile> drivers = driverService.getAvailableDrivers();
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllDrivers() {
        List<DriverProfile> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }
}