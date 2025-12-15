package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.OwnerProfileDTO;
import com.fooddelivery.userservice.exception.UnauthorizedException;
import com.fooddelivery.userservice.service.OwnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminOwnerController {

    private final OwnerService ownerService;

    public AdminOwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
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

    @PostMapping("/{userId}/owner/profile")
    public ResponseEntity<?> createOwnerProfileForUser(
            @PathVariable Long userId,
            @Valid @RequestBody OwnerProfileDTO dto,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        dto.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ownerService.createOwnerProfile(dto));
    }

    @PutMapping("/{userId}/owner/profile")
    public ResponseEntity<?> updateOwnerProfileForUser(
            @PathVariable Long userId,
            @Valid @RequestBody OwnerProfileDTO dto,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        dto.setUserId(userId);
        return ResponseEntity.ok(ownerService.updateOwnerProfile(userId, dto));
    }

    @GetMapping("/{userId}/owner/profile")
    public ResponseEntity<?> getOwnerProfileForUser(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        return ResponseEntity.ok(ownerService.getOwnerProfile(userId));
    }

    @DeleteMapping("/{userId}/owner/profile")
    public ResponseEntity<?> deleteOwnerProfileForUser(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        ownerService.deleteOwnerProfile(userId);
        return ResponseEntity.ok(Map.of("message", "Owner profile deleted successfully"));
    }
}
