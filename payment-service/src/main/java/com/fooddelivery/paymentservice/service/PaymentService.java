package com.fooddelivery.paymentservice.service;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.fooddelivery.paymentservice.dto.PaymentRequest;
import com.fooddelivery.paymentservice.event.*;
import com.fooddelivery.paymentservice.dto.PaymentResponse;
import com.fooddelivery.paymentservice.exception.PaymentNotFoundException;
import com.fooddelivery.paymentservice.exception.UnauthorizedException;
import com.fooddelivery.paymentservice.feign.OrderClient;
import com.fooddelivery.paymentservice.feign.OrderResponse;
import com.fooddelivery.paymentservice.models.Payment;
import com.fooddelivery.paymentservice.models.PaymentStatus;
import com.fooddelivery.paymentservice.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final OrderClient orderClient;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(PaymentRepository repository, OrderClient orderClient, PaymentEventPublisher eventPublisher) {
        this.repository = repository;
        this.orderClient = orderClient;
        this.eventPublisher = eventPublisher;
    }

    // CREATE PAYMENT (CUSTOMER)
    public PaymentResponse createPayment(PaymentRequest request, Long userId, String role) {
        // Validate input
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("OrderId is required");
        }

        // Check for duplicate payment
        if (repository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new IllegalStateException("Payment already exists for this order");
        }

        // Fetch order details from Order Service to get the actual amount
        OrderResponse order;
        try {
            order = orderClient.getOrder(request.getOrderId(), userId, role);
        } catch (Exception e) {
            log.error("Failed to fetch order {}: {}", request.getOrderId(), e.getMessage());
            throw new IllegalStateException("Failed to fetch order details. Please try again.");
        }

        // Validate that the order belongs to the user
        if (!order.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("This order does not belong to you");
        }

        // Validate order amount
        if (order.getTotal() == null || order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Invalid order amount");
        }

        // Validate order status (optional - you might want to check if order is in a payable state)
        if ("CANCELLED".equalsIgnoreCase(order.getStatus()) || "PAID".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Order is not in a payable state");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(order.getTotal());
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
            log.error("Failed to notify order service: {}", e.getMessage());
        }
        // PUBLISH EVENT
        try {
            eventPublisher.publishPaymentConfirmed(new PaymentConfirmedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount()
            ));
        } catch (Exception e) {
            log.error("Failed to publish PAYMENT_CONFIRMED event: {}", e.getMessage());
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
        // PUBLISH FAILED EVENT
        try {
            eventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    "Cancelled by customer"
            ));
        } catch (Exception e) {
            log.error("Failed to publish PAYMENT_FAILED event: {}", e.getMessage());
        }
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

        // PUBLISH EVENT
        try {
            eventPublisher.publishPaymentRefunded(new PaymentRefundedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount()
            ));
        } catch (Exception e) {
            log.error("Failed to publish PAYMENT_REFUNDED event: {}", e.getMessage());
        }

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
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus().name(),
                message,
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}