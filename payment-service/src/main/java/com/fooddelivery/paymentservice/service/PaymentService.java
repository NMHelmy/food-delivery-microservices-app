package com.fooddelivery.paymentservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fooddelivery.paymentservice.dto.PaymentRequest;
import com.fooddelivery.paymentservice.dto.PaymentResponse;
import com.fooddelivery.paymentservice.exception.PaymentNotFoundException;
import com.fooddelivery.paymentservice.exception.UnauthorizedException;
import com.fooddelivery.paymentservice.feign.OrderClient;
import com.fooddelivery.paymentservice.models.Payment;
import com.fooddelivery.paymentservice.models.PaymentStatus;
import com.fooddelivery.paymentservice.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final OrderClient orderClient;

    public PaymentService(PaymentRepository repository, OrderClient orderClient) {
        this.repository = repository;
        this.orderClient = orderClient;
    }

    // CREATE PAYMENT (CUSTOMER)
    public PaymentResponse createPayment(PaymentRequest request, Long userId) {
        // Validate input
        if (request.getOrderId() == null || request.getAmount() == null) {
            throw new IllegalArgumentException("OrderId and amount are required");
        }
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // Check for duplicate payment
        if (repository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new IllegalStateException("Payment already exists for this order");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setUserId(userId);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        repository.save(payment);
        return mapToResponse(payment, "Payment created");
    }

    // CONFIRM PAYMENT (CUSTOMER)
    public PaymentResponse confirmPayment(Long paymentId, Long userId) {
        Payment payment = getPaymentOrThrow(paymentId);

        // Authorization check
        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedException("Not your payment");
        }

        // State validation
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be confirmed");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        repository.save(payment);

        // Notify order service
        try {
            orderClient.markOrderAsPaid(payment.getOrderId());
        } catch (Exception e) {
            // Log error but don't fail the payment
            System.err.println("Failed to notify order service: " + e.getMessage());
        }

        return mapToResponse(payment, "Payment successful");
    }

    // GET PAYMENT BY ID (CUSTOMER / ADMIN)
    public PaymentResponse getPaymentById(Long paymentId, Long userId, String role) {
        Payment payment = getPaymentOrThrow(paymentId);
        authorizeAccess(payment, userId, role);
        return mapToResponse(payment, "Payment retrieved");
    }

    // GET PAYMENT BY ORDER ID (CUSTOMER / ADMIN)
    public PaymentResponse getPaymentByOrderId(Long orderId, Long userId, String role) {
        Payment payment = repository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
        authorizeAccess(payment, userId, role);
        return mapToResponse(payment, "Payment retrieved");
    }

    // CANCEL PAYMENT (CUSTOMER)
    public PaymentResponse cancelPayment(Long paymentId, Long userId) {
        Payment payment = getPaymentOrThrow(paymentId);

        // Authorization check
        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedException("Not your payment");
        }

        // State validation
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        repository.save(payment);
        return mapToResponse(payment, "Payment cancelled");
    }

    // REFUND PAYMENT (ADMIN)
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = getPaymentOrThrow(paymentId);

        // State validation
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        repository.save(payment);
        return mapToResponse(payment, "Payment refunded");
    }

    // GET PAYMENTS FOR LOGGED-IN CUSTOMER
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(p -> mapToResponse(p, ""))
                .toList();
    }

    // GET ALL PAYMENTS (ADMIN)
    public List<PaymentResponse> getAllPayments() {
        return repository.findAll()
                .stream()
                .map(p -> mapToResponse(p, ""))
                .toList();
    }

    // ----------------- HELPERS -----------------
    private Payment getPaymentOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
    }

    private void authorizeAccess(Payment payment, Long userId, String role) {
        if ("CUSTOMER".equalsIgnoreCase(role)
                && !payment.getUserId().equals(userId)) {
            throw new UnauthorizedException("Not your payment");
        }
    }

    private PaymentResponse mapToResponse(Payment payment, String message) {
        return new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                message
        );
    }
}
