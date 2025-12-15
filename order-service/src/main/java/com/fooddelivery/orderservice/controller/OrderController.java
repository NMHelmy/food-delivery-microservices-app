package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.*;
import com.fooddelivery.orderservice.exception.UnauthorizedException;
import com.fooddelivery.orderservice.model.OrderStatus;
import com.fooddelivery.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

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

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody CreateOrderDTO dto,
            HttpServletRequest request) {

        validateUserRole(request, "CUSTOMER");
        Long customerId = getUserIdFromHeader(request);

        OrderResponseDTO order = orderService.createOrder(dto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        OrderResponseDTO order = orderService.getOrderById(orderId);

        // Verify user has access to this order
        boolean hasAccess = userId.equals(order.getCustomerId()) ||
                userId.equals(order.getDriverId()) ||
                "ADMIN".equals(role);

        if (!hasAccess) {
            throw new UnauthorizedException("You don't have access to this order");
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(HttpServletRequest request) {
        validateUserRole(request, "ADMIN");
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomerId(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        if (!userId.equals(customerId) && !"ADMIN".equals(role)) {
            throw new UnauthorizedException("You can only view your own orders");
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByRestaurantId(
            @PathVariable Long restaurantId,
            HttpServletRequest request) {

        String role = getUserRoleFromHeader(request);

        if (!"RESTAURANT_OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only restaurant owners can view restaurant orders");
        }

        // TODO: Verify user owns this restaurant
        List<OrderResponseDTO> orders = orderService.getOrdersByRestaurantId(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDriverId(
            @PathVariable Long driverId,
            HttpServletRequest request) {

        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        if (!userId.equals(driverId) && !"ADMIN".equals(role)) {
            throw new UnauthorizedException("You can only view your own orders");
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByDriverId(driverId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<List<OrderResponseDTO>> getActiveOrdersForDriver(
            @PathVariable Long driverId,
            HttpServletRequest request) {

        Long userId = getUserIdFromHeader(request);

        if (!userId.equals(driverId)) {
            throw new UnauthorizedException("You can only view your own active orders");
        }

        List<OrderResponseDTO> orders = orderService.getActiveOrdersForDriver(driverId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            HttpServletRequest request) {

        validateUserRole(request, "ADMIN");
        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDTO dto,
            HttpServletRequest request) {

        getUserIdFromHeader(request); // Verify authenticated
        // TODO: Add role-based validation

        OrderResponseDTO order = orderService.updateOrderStatus(orderId, dto);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/assign-driver")
    public ResponseEntity<OrderResponseDTO> assignDriver(
            @PathVariable Long orderId,
            @Valid @RequestBody AssignDriverDTO dto,
            HttpServletRequest request) {

        String role = getUserRoleFromHeader(request);

        if (!"ADMIN".equals(role) && !"RESTAURANT_OWNER".equals(role)) {
            throw new UnauthorizedException("Only admins and restaurant owners can assign drivers");
        }

        OrderResponseDTO order = orderService.assignDriver(orderId, dto);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<OrderResponseDTO> updatePaymentStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentStatusDTO dto,
            HttpServletRequest request) {

        validateUserRole(request, "ADMIN");
        OrderResponseDTO order = orderService.updatePaymentStatus(orderId, dto);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long userId = getUserIdFromHeader(request);
        OrderResponseDTO order = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(order);
    }
}