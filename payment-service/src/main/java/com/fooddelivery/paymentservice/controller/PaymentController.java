package com.fooddelivery.paymentservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.paymentservice.dto.PaymentRequest;
import com.fooddelivery.paymentservice.dto.PaymentResponse;
import com.fooddelivery.paymentservice.exception.UnauthorizedException;
import com.fooddelivery.paymentservice.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

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

    // POST /payments (CUSTOMER)
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromHeader(httpRequest);
        String role = getUserRoleFromHeader(httpRequest);
        PaymentResponse response = service.createPayment(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /payments/{id}/confirm (CUSTOMER)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        PaymentResponse response = service.confirmPayment(id, userId);
        return ResponseEntity.ok(response);
    }

    // GET /payments/{id} (CUSTOMER / ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);
        PaymentResponse response = service.getPaymentById(id, userId, role);
        return ResponseEntity.ok(response);
    }

    // GET /payments/order/{orderId} (CUSTOMER / ADMIN)
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);
        PaymentResponse response = service.getPaymentByOrderId(orderId, userId, role);
        return ResponseEntity.ok(response);
    }

    // POST /payments/{id}/cancel (CUSTOMER)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        PaymentResponse response = service.cancelPayment(id, userId);
        return ResponseEntity.ok(response);
    }

    // POST /payments/{id}/refund (ADMIN ONLY)
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            HttpServletRequest request) {
        validateUserRole(request, "ADMIN");
        PaymentResponse response = service.refundPayment(id);
        return ResponseEntity.ok(response);
    }

    // GET /payments/customer (CUSTOMER - their own payments)
    @GetMapping("/customer")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        String role = getUserRoleFromHeader(request);

        if (!"CUSTOMER".equals(role)) {
            throw new UnauthorizedException("Only customers can access this endpoint");
        }

        List<PaymentResponse> payments = service.getPaymentsByUser(userId);
        return ResponseEntity.ok(payments);
    }

    // GET /payments/customer/{customerId} (ADMIN - specific customer's payments)
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomerId(
            @PathVariable Long customerId,
            HttpServletRequest request) {
        validateUserRole(request, "ADMIN");
        List<PaymentResponse> payments = service.getPaymentsByUser(customerId);
        return ResponseEntity.ok(payments);
    }

    // GET /payments (ADMIN ONLY)
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            HttpServletRequest request) {
        validateUserRole(request, "ADMIN");
        List<PaymentResponse> payments = service.getAllPayments();
        return ResponseEntity.ok(payments);
    }
}
