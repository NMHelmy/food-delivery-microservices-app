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

    @GetMapping("/customer")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(HttpServletRequest request) {
        Long customerId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        if (!"CUSTOMER".equals(role)) {
            throw new UnauthorizedException("Only customers can access customer orders");
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomerId(
            @PathVariable Long customerId,
            HttpServletRequest request) {
        validateUserRole(request, "ADMIN");

        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByRestaurantId(
            @PathVariable Long restaurantId,
            HttpServletRequest request) {

        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        if (!"RESTAURANT_OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only restaurant owners can view restaurant orders");
        }

        // Verify restaurant owner actually owns this restaurant (unless admin)
        if ("RESTAURANT_OWNER".equals(role)) {
            boolean ownsRestaurant = orderService.verifyRestaurantOwnership(restaurantId, userId);
            if (!ownsRestaurant) {
                throw new UnauthorizedException("You can only view orders for your own restaurant");
            }
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByRestaurantId(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/driver")
    public ResponseEntity<List<OrderResponseDTO>> getMyDriverOrders(HttpServletRequest request) {
        Long driverId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        if (!"DELIVERY_DRIVER".equals(role)) {
            throw new UnauthorizedException("Only drivers can access driver orders");
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByDriverId(driverId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDriverId(
            @PathVariable Long driverId,
            HttpServletRequest request) {
        validateUserRole(request, "ADMIN");

        List<OrderResponseDTO> orders = orderService.getOrdersByDriverId(driverId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/driver/active")
    public ResponseEntity<List<OrderResponseDTO>> getMyActiveOrders(HttpServletRequest request) {
        Long driverId = getUserIdFromHeader(request);
        validateUserRole(request, "DELIVERY_DRIVER");

        List<OrderResponseDTO> orders = orderService.getActiveOrdersForDriver(driverId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<List<OrderResponseDTO>> getActiveOrdersForDriver(
            @PathVariable Long driverId,
            HttpServletRequest request) {
        validateUserRole(request, "ADMIN");

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

        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        // Get the order to check ownership/permissions
        OrderResponseDTO order = orderService.getOrderById(orderId);

        // Role-based validation for status updates
        boolean canUpdate = false;

        switch (role) {
            case "ADMIN":
                canUpdate = true; // Admins can update any order
                break;
            case "RESTAURANT_OWNER":
                // Restaurant owner can update if they own the restaurant
                canUpdate = orderService.verifyRestaurantOwnership(order.getRestaurantId(), userId);
                break;
            case "DELIVERY_DRIVER":
                // Driver can update only their assigned orders
                canUpdate = userId.equals(order.getDriverId());
                break;
            case "CUSTOMER":
                // Customer can only cancel (which should use cancelOrder endpoint)
                throw new UnauthorizedException("Customers cannot update order status directly");
            default:
                canUpdate = false;
        }

        if (!canUpdate) {
            throw new UnauthorizedException("You don't have permission to update this order");
        }

        OrderResponseDTO updatedOrder  = orderService.updateOrderStatus(orderId, dto);
        return ResponseEntity.ok(updatedOrder);
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