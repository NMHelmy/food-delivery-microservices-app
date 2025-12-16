package com.fooddelivery.deliveryservice.controller;

import com.fooddelivery.deliveryservice.dto.*;
import com.fooddelivery.deliveryservice.exception.UnauthorizedException;
import com.fooddelivery.deliveryservice.service.DeliveryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

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

    private void validateUserRole(HttpServletRequest request, String requiredRole) {
        String role = getUserRoleFromHeader(request);
        if (!role.equals(requiredRole)) {
            throw new UnauthorizedException("Unauthorized: Required role is " + requiredRole);
        }
    }

    // Create delivery (triggered when order is ready)
    @PostMapping
    public ResponseEntity<DeliveryResponseDTO> createDelivery(
            @Valid @RequestBody CreateDeliveryDTO dto,
            HttpServletRequest request) {
        // Only admins or restaurant owners can create deliveries
        String role = getUserRoleFromHeader(request);
        if (!"ADMIN".equals(role) && !"RESTAURANT_OWNER".equals(role)) {
            throw new UnauthorizedException("Only admins and restaurant owners can create deliveries");
        }

        DeliveryResponseDTO delivery = deliveryService.createDelivery(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }

    // Get delivery by ID
    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponseDTO> getDeliveryById(
            @PathVariable Long deliveryId,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        DeliveryResponseDTO delivery = deliveryService.getDeliveryById(deliveryId);

        // Verify user has access
        boolean hasAccess = userId.equals(delivery.getCustomerId()) ||
                userId.equals(delivery.getDriverId()) ||
                "ADMIN".equals(role);

        if (!hasAccess) {
            throw new UnauthorizedException("You don't have access to this delivery");
        }

        return ResponseEntity.ok(delivery);
    }

    // Get delivery by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponseDTO> getDeliveryByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        getUserIdFromHeader(request); // Verify authenticated
        DeliveryResponseDTO delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(delivery);
    }

    // Update delivery status
    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<DeliveryResponseDTO> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusDTO dto,
            HttpServletRequest request) {
        getUserIdFromHeader(request); // Verify authenticated
        DeliveryResponseDTO delivery = deliveryService.updateDeliveryStatus(deliveryId, dto);
        return ResponseEntity.ok(delivery);
    }

    // Assign driver to delivery
    @PostMapping("/{deliveryId}/assign-driver")
    public ResponseEntity<DeliveryResponseDTO> assignDriver(
            @PathVariable Long deliveryId,
            @Valid @RequestBody AssignDriverDTO dto,
            HttpServletRequest request) {
        String role = getUserRoleFromHeader(request);
        if (!"ADMIN".equals(role) && !"RESTAURANT_OWNER".equals(role)) {
            throw new UnauthorizedException("Only admins and restaurant owners can assign drivers");
        }

        DeliveryResponseDTO delivery = deliveryService.assignDriver(deliveryId, dto);
        return ResponseEntity.ok(delivery);
    }

    // Driver confirms pickup from restaurant
    @PutMapping("/{deliveryId}/pickup-confirmation")
    public ResponseEntity<DeliveryResponseDTO> confirmPickup(
            @PathVariable Long deliveryId,
            HttpServletRequest request) {
        validateUserRole(request, "DRIVER");
        Long driverId = getUserIdFromHeader(request);

        DeliveryResponseDTO delivery = deliveryService.confirmPickup(deliveryId, driverId);
        return ResponseEntity.ok(delivery);
    }

    // Driver confirms delivery to customer
    @PutMapping("/{deliveryId}/delivery-confirmation")
    public ResponseEntity<DeliveryResponseDTO> confirmDelivery(
            @PathVariable Long deliveryId,
            HttpServletRequest request) {
        validateUserRole(request, "DRIVER");
        Long driverId = getUserIdFromHeader(request);

        DeliveryResponseDTO delivery = deliveryService.confirmDelivery(deliveryId, driverId);
        return ResponseEntity.ok(delivery);
    }

    // Update driver location
    @PutMapping("/{deliveryId}/driver-location")
    public ResponseEntity<DeliveryResponseDTO> updateDriverLocation(
            @PathVariable Long deliveryId,
            @Valid @RequestBody UpdateDriverLocationDTO dto,
            HttpServletRequest request) {
        validateUserRole(request, "DRIVER");
        Long driverId = getUserIdFromHeader(request);

        DeliveryResponseDTO delivery = deliveryService.updateDriverLocation(deliveryId, dto, driverId);
        return ResponseEntity.ok(delivery);
    }

    // Get customer's delivery history
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getDeliveriesByCustomerId(
            @PathVariable Long customerId,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        if (!userId.equals(customerId) && !"ADMIN".equals(role)) {
            throw new UnauthorizedException("You can only view your own delivery history");
        }

        List<DeliveryResponseDTO> deliveries = deliveryService.getDeliveriesByCustomerId(customerId);
        return ResponseEntity.ok(deliveries);
    }

    // Get driver's delivery history
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getDeliveriesByDriverId(
            @PathVariable Long driverId,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        if (!userId.equals(driverId) && !"ADMIN".equals(role)) {
            throw new UnauthorizedException("You can only view your own delivery history");
        }

        List<DeliveryResponseDTO> deliveries = deliveryService.getDeliveriesByDriverId(driverId);
        return ResponseEntity.ok(deliveries);
    }

    // Get restaurant's delivery history
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getDeliveriesByRestaurantId(
            @PathVariable Long restaurantId,
            HttpServletRequest request) {
        String role = getUserRoleFromHeader(request);
        if (!"RESTAURANT_OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only restaurant owners can view restaurant deliveries");
        }

        List<DeliveryResponseDTO> deliveries = deliveryService.getDeliveriesByRestaurantId(restaurantId);
        return ResponseEntity.ok(deliveries);
    }
}
