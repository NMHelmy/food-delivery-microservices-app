package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.*;
import com.fooddelivery.orderservice.model.OrderStatus;
import com.fooddelivery.orderservice.service.OrderService;
// import com.fooddelivery.cartservice.dto.*;
import com.fooddelivery.orderservice.dto.CreateOrderFromCartDTO;
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

    // CUSTOMER

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody CreateOrderDTO dto,
            @RequestHeader("X-User-Id") Long customerId) {

        OrderResponseDTO order = orderService.createOrder(dto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/customer")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(
            @RequestHeader("X-User-Id") Long customerId) {

        return ResponseEntity.ok(
                orderService.getOrdersByCustomerId(customerId)
        );
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long customerId) {

        OrderResponseDTO order = orderService.cancelOrder(orderId, customerId);
        return ResponseEntity.ok(order);
    }

    // ADMIN / CUSTOMER (ownership handled in service)

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        OrderResponseDTO order =
                orderService.getOrderById(orderId, userId, userRole);

        return ResponseEntity.ok(order);
    }

    // ADMIN

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomerId(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                orderService.getOrdersByCustomerId(customerId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(
            @PathVariable OrderStatus status) {

        return ResponseEntity.ok(
                orderService.getOrdersByStatus(status)
        );
    }

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<OrderResponseDTO> updatePaymentStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentStatusDTO dto) {

        return ResponseEntity.ok(
                orderService.updatePaymentStatus(orderId, dto)
        );
    }

    // ADMIN / RESTAURANT_OWNER

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByRestaurantId(
            @PathVariable Long restaurantId,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(
                orderService.getOrdersByRestaurantId(restaurantId)
        );
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDTO dto,
            @RequestHeader("X-User-Id") Long userId) {

        OrderResponseDTO updatedOrder =
                orderService.updateOrderStatus(orderId, dto);

        return ResponseEntity.ok(updatedOrder);
    }

    // INTERNAL - Payment calls this endpoint
    @PostMapping("/{orderId}/paid")
    public ResponseEntity<Void> markOrderAsPaid(@PathVariable Long orderId) {
        orderService.markOrderAsPaid(orderId);
        return ResponseEntity.ok().build();
    }

    // INTERNAL - Cart service calls this endpoint
    @PostMapping("/from-cart")
    public ResponseEntity<OrderResponseDTO> createOrderFromCart(
            @Valid @RequestBody CreateOrderFromCartDTO dto,
            @RequestHeader("X-Internal-Request") String internalFlag) {
        if (!"true".equals(internalFlag)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        OrderResponseDTO order = orderService.createOrderFromCart(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

}
