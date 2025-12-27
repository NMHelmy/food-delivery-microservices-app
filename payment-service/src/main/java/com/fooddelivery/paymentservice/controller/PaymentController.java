package com.fooddelivery.paymentservice.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fooddelivery.paymentservice.dto.PaymentRequest;
import com.fooddelivery.paymentservice.dto.PaymentResponse;
import com.fooddelivery.paymentservice.exception.UnauthorizedException;
import com.fooddelivery.paymentservice.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /* =========================
       🔐 Header Helpers
       ========================= */

    private Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            throw new UnauthorizedException("Missing X-User-Id header");
        }
        return Long.parseLong(userId);
    }

    private String getUserRole(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role == null) {
            throw new UnauthorizedException("Missing X-User-Role header");
        }
        return role;
    }

    private void requireRole(HttpServletRequest request, String role) {
        if (!role.equals(getUserRole(request))) {
            throw new UnauthorizedException("Unauthorized: requires " + role);
        }
    }

    /* =========================
       💳 Create Payment
       ========================= */

    /**
     * CUSTOMER creates a payment.
     * Amount is fetched ONLY from order-service.
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);

        PaymentResponse response =
                paymentService.createPayment(
                        request.getOrderId(),
                        request.getPaymentMethod(),
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /* =========================
       ✅ Confirm Payment
       ========================= */

    /**
     * CUSTOMER confirms their own payment.
     */
    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long paymentId,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);

        return ResponseEntity.ok(
                paymentService.confirmPayment(paymentId, userId)
        );
    }

    /* =========================
       ❌ Cancel Payment
       ========================= */

    /**
     * CUSTOMER cancels their own pending payment.
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long paymentId,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);

        return ResponseEntity.ok(
                paymentService.cancelPayment(paymentId, userId)
        );
    }

    /* =========================
       💸 Refund Payment
       ========================= */

    /**
     * ADMIN refunds a confirmed payment.
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long paymentId,
            HttpServletRequest request
    ) {
        requireRole(request, "ADMIN");

        return ResponseEntity.ok(
                paymentService.refundPayment(paymentId)
        );
    }

    /* =========================
       🔍 Get Payment
       ========================= */

    /**
     * CUSTOMER gets their own payment.
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId, userId)
        );
    }

    /**
     * CUSTOMER gets their own payment by orderId.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId, userId)
        );
    }

    /* =========================
       📋 Lists
       ========================= */

    /**
     * CUSTOMER gets their own payments.
     */
    @GetMapping("/customer")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);
        requireRole(request, "CUSTOMER");

        return ResponseEntity.ok(
                paymentService.getPaymentsByUser(userId)
        );
    }

    /**
     * ADMIN gets all payments.
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            HttpServletRequest request
    ) {
        requireRole(request, "ADMIN");

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }
}
