package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.OwnerProfileDTO;
import com.fooddelivery.userservice.model.OwnerProfile;
import com.fooddelivery.userservice.service.OwnerService;
import com.fooddelivery.userservice.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
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

    private void validateOwnerRole(HttpServletRequest request) {
        String role = getUserRoleFromHeader(request);
        if (!"RESTAURANT_OWNER".equals(role)) {
            throw new UnauthorizedException("Only RESTAURANT_OWNER role can access this endpoint");
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(
            @Valid @RequestBody OwnerProfileDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateOwnerRole(request);

            dto.setUserId(userId);

            OwnerProfile profile = ownerService.createOwnerProfile(dto);
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
            validateOwnerRole(request);

            OwnerProfile profile = ownerService.getOwnerProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody OwnerProfileDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateOwnerRole(request);

            OwnerProfile profile = ownerService.updateOwnerProfile(userId, dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        try {
            OwnerProfile profile = ownerService.getOwnerProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}