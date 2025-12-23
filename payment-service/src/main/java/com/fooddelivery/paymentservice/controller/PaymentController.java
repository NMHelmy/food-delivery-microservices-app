package com.fooddelivery.paymentservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fooddelivery.paymentservice.dto.PaymentRequest;
import com.fooddelivery.paymentservice.dto.PaymentResponse;
import com.fooddelivery.paymentservice.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    // CUSTOMER

    // POST /payments
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        PaymentResponse response =
                service.createPayment(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /payments/{id}/confirm
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        PaymentResponse response =
                service.confirmPayment(id, userId);

        return ResponseEntity.ok(response);
    }

    // POST /payments/{id}/cancel
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        PaymentResponse response =
                service.cancelPayment(id, userId);

        return ResponseEntity.ok(response);
    }

    // GET /payments/customer
    @GetMapping("/customer")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @RequestHeader("X-User-Id") Long userId) {

        List<PaymentResponse> payments =
                service.getPaymentsByUser(userId);

        return ResponseEntity.ok(payments);
    }

    // CUSTOMER or ADMIN

    // GET /payments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        PaymentResponse response =
                service.getPaymentById(id, userId);

        return ResponseEntity.ok(response);
    }

    // GET /payments/order/{orderId}
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId) {

        PaymentResponse response =
                service.getPaymentByOrderId(orderId, userId);

        return ResponseEntity.ok(response);
    }

    // ADMIN

    // POST /payments/{id}/refund
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id) {

        PaymentResponse response =
                service.refundPayment(id);

        return ResponseEntity.ok(response);
    }

    // GET /payments/customer/{customerId}
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomerId(
            @PathVariable Long customerId) {

        List<PaymentResponse> payments =
                service.getPaymentsByUser(customerId);

        return ResponseEntity.ok(payments);
    }

    // GET /payments
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        List<PaymentResponse> payments =
                service.getAllPayments();

        return ResponseEntity.ok(payments);
    }
}
