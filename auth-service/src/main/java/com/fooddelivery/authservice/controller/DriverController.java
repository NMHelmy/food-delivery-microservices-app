package com.fooddelivery.authservice.controller;

import com.fooddelivery.authservice.dto.UpdateDriverProfileDTO;
import com.fooddelivery.authservice.model.User;
import com.fooddelivery.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    @Autowired
    private UserService userService;

    // ADMIN / INTERNAL

    @GetMapping("/available")
    public ResponseEntity<List<User>> getAvailableDrivers() {
        return ResponseEntity.ok(
                userService.getAvailableDrivers()
        );
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<Map<String, Object>> getDriverProfile(
            @PathVariable Long driverId) {

        User driver = userService.getUserById(driverId);

        return ResponseEntity.ok(
                Map.of(
                        "userId", driver.getId(),
                        "fullName", driver.getFullName(),
                        "email", driver.getEmail(),
                        "phoneNumber", driver.getPhoneNumber(),
                        "vehicleType", driver.getVehicleType(),
                        "vehicleNumber", driver.getVehicleNumber(),
                        "driverStatus", driver.getDriverStatus()
                )
        );
    }

    // DELIVERY DRIVER

    @PutMapping("/my-profile")
    public ResponseEntity<User> updateMyProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateDriverProfileDTO dto) {

        User driver = userService.updateDriverProfile(
                userId,
                dto.getVehicleType(),
                dto.getVehicleNumber(),
                dto.getDriverStatus()
        );

        return ResponseEntity.ok(driver);
    }

    @PutMapping("/{driverId}/status")
    public ResponseEntity<Map<String, String>> updateDriverStatus(
            @PathVariable Long driverId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> statusUpdate) {

        String status = statusUpdate.get("status");

        User driver = userService.updateDriverStatus(
                driverId,
                userId,
                status
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Driver status updated",
                        "status", driver.getDriverStatus().toString()
                )
        );
    }

}
