package com.fooddelivery.authservice.controller;

import com.fooddelivery.authservice.dto.UpdateDriverProfileDTO;
import com.fooddelivery.authservice.model.User;
import com.fooddelivery.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    @Autowired
    private UserService userService;

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDrivers() {
        List<User> drivers = userService.getAvailableDrivers();
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<?> getDriverProfile(@PathVariable Long driverId) {
        User driver = userService.getUserById(driverId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", driver.getId());
        response.put("fullName", driver.getFullName());
        response.put("email", driver.getEmail());
        response.put("phoneNumber", driver.getPhoneNumber());
        response.put("vehicleType", driver.getVehicleType());
        response.put("vehicleNumber", driver.getVehicleNumber());
        response.put("driverStatus", driver.getDriverStatus());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/my-profile")
    public ResponseEntity<?> updateMyProfile(
            @RequestHeader("X-User-Id") String userIdHeader,
            @Valid @RequestBody UpdateDriverProfileDTO dto) {

        Long userId = Long.parseLong(userIdHeader);
        User driver = userService.updateDriverProfile(
                userId,
                dto.getVehicleType(),
                dto.getVehicleNumber(),
                dto.getDriverStatus()
        );

        return ResponseEntity.ok(driver);
    }

    @PutMapping("/{driverId}/status")
    public ResponseEntity<?> updateDriverStatus(
            @PathVariable Long driverId,
            @RequestBody Map<String, String> statusUpdate) {

        String status = statusUpdate.get("status");
        User driver = userService.updateDriverProfile(driverId, null, null, status);

        return ResponseEntity.ok(Map.of(
                "message", "Driver status updated",
                "status", driver.getDriverStatus()
        ));
    }
}