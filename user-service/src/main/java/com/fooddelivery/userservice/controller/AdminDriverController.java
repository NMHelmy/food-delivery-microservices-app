package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.DriverProfileDTO;
import com.fooddelivery.userservice.exception.UnauthorizedException;
import com.fooddelivery.userservice.service.DriverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminDriverController {

    private final DriverService driverService;

    public AdminDriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    private String getUserRoleFromHeader(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role == null || role.isEmpty()) {
            throw new UnauthorizedException("User role not found in request headers");
        }
        return role;
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = getUserRoleFromHeader(request);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Admin access required");
        }
    }

    @PostMapping("/{userId}/driver/profile")
    public ResponseEntity<?> createDriverProfileForUser(
            @PathVariable Long userId,
            @Valid @RequestBody DriverProfileDTO dto,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        dto.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.createDriverProfile(dto));
    }

    @PutMapping("/{userId}/driver/profile")
    public ResponseEntity<?> updateDriverProfileForUser(
            @PathVariable Long userId,
            @Valid @RequestBody DriverProfileDTO dto,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        dto.setUserId(userId);
        return ResponseEntity.ok(driverService.updateDriverProfile(userId, dto));
    }

    @GetMapping("/{userId}/driver/profile")
    public ResponseEntity<?> getDriverProfileForUser(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        return ResponseEntity.ok(driverService.getDriverProfile(userId));
    }

    @DeleteMapping("/{userId}/driver/profile")
    public ResponseEntity<?> deleteDriverProfileForUser(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        driverService.deleteDriverProfile(userId);
        return ResponseEntity.ok(Map.of("message", "Driver profile deleted successfully"));
    }
}
