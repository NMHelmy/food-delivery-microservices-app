package com.fooddelivery.paymentservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(service.createPayment(request));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.confirmPayment(id));
    }
}
