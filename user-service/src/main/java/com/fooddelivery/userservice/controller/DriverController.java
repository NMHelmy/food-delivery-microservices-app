package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.DriverProfileDTO;
import com.fooddelivery.userservice.model.DriverProfile;
import com.fooddelivery.userservice.service.DriverService;
import com.fooddelivery.userservice.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
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

    private Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            throw new UnauthorizedException("User ID not found in request headers");
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid User ID format");
        }
    }

    private String getUserRoleFromHeader(HttpServletRequest request) {
        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader == null || roleHeader.isEmpty()) {
            throw new UnauthorizedException("User role not found in request headers");
        }
        return roleHeader;
    }

    private void validateDriverRole(HttpServletRequest request) {
        String role = getUserRoleFromHeader(request);
        if (!"DELIVERY_DRIVER".equals(role)) {
            throw new UnauthorizedException("Only DELIVERY_DRIVER role can access this endpoint");
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(
            @Valid @RequestBody DriverProfileDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateDriverRole(request);

            dto.setUserId(userId);

            DriverProfile profile = driverService.createDriverProfile(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateDriverRole(request);

            DriverProfile profile = driverService.getDriverProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody DriverProfileDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateDriverRole(request);

            DriverProfile profile = driverService.updateDriverProfile(userId, dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromHeader(httpRequest);
            validateDriverRole(httpRequest);

            String status = request.get("status");
            DriverProfile profile = driverService.updateDriverStatus(userId, status);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/location")
    public ResponseEntity<?> updateLocation(
            @RequestBody Map<String, Double> request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromHeader(httpRequest);
            validateDriverRole(httpRequest);

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
    public ResponseEntity<?> getAllDrivers(HttpServletRequest request) {
        try {
            String role = getUserRoleFromHeader(request);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can view all drivers"));
            }

            List<DriverProfile> drivers = driverService.getAllDrivers();
            return ResponseEntity.ok(drivers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // This endpoint is for internal service-to-service communication.
    // It does NOT require authentication headers.
    // WARNING: Should NOT be exposed through API Gateway!
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        try {
            DriverProfile profile = driverService.getDriverProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}