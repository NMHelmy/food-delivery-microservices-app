package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.CustomerProfileDTO;
import com.fooddelivery.userservice.exception.UnauthorizedException;
import com.fooddelivery.userservice.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminCustomerController {

    private final CustomerService customerService;

    public AdminCustomerController(CustomerService customerService) {
        this.customerService = customerService;
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

    @PostMapping("/{userId}/customer/profile")
    public ResponseEntity<?> createCustomerProfileForUser(
            @PathVariable Long userId,
            @Valid @RequestBody CustomerProfileDTO dto,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        dto.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomerProfile(dto));
    }

    @PutMapping("/{userId}/customer/profile")
    public ResponseEntity<?> updateCustomerProfileForUser(
            @PathVariable Long userId,
            @Valid @RequestBody CustomerProfileDTO dto,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        dto.setUserId(userId);
        return ResponseEntity.ok(customerService.updateCustomerProfile(userId, dto));
    }

    @GetMapping("/{userId}/customer/profile")
    public ResponseEntity<?> getCustomerProfileForUser(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        return ResponseEntity.ok(customerService.getCustomerProfile(userId));
    }

    @DeleteMapping("/{userId}/customer/profile")
    public ResponseEntity<?> deleteCustomerProfileForUser(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        requireAdmin(request);

        customerService.deleteCustomerProfile(userId);
        return ResponseEntity.ok(Map.of("message", "Customer profile deleted successfully"));
    }
}
