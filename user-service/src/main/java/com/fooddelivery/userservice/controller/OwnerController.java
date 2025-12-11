package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.OwnerProfileDTO;
import com.fooddelivery.userservice.feign.AuthServiceClient;
import com.fooddelivery.userservice.model.OwnerProfile;
import com.fooddelivery.userservice.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

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
            @Valid @RequestBody OwnerProfileDTO dto) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = validateTokenAndGetUserId(token);

            dto.setUserId(userId);

            OwnerProfile profile = ownerService.createOwnerProfile(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {  // ADDED
        try {
            String token = authHeader.replace("Bearer ", "");
            Long requestingUserId = validateTokenAndGetUserId(token);

            // ADDED: Only allow users to view their own profile
            if (!requestingUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only view your own profile"));
            }

            OwnerProfile profile = ownerService.getOwnerProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody OwnerProfileDTO dto,
            @RequestHeader("Authorization") String authHeader) {  // ADDED
        try {
            String token = authHeader.replace("Bearer ", "");
            Long requestingUserId = validateTokenAndGetUserId(token);

            // ADDED: Only allow users to update their own profile
            if (!requestingUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only update your own profile"));
            }

            OwnerProfile profile = ownerService.updateOwnerProfile(userId, dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}