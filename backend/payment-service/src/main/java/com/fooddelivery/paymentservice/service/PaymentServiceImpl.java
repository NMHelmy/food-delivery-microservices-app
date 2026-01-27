package com.fooddelivery.paymentservice.service;

import com.fooddelivery.paymentservice.dto.PaymentResponse;
import com.fooddelivery.paymentservice.event.PaymentConfirmedEvent;
import com.fooddelivery.paymentservice.event.PaymentEventPublisher;
import com.fooddelivery.paymentservice.event.PaymentRefundedEvent;
import com.fooddelivery.paymentservice.exception.PaymentConflictException;
import com.fooddelivery.paymentservice.exception.PaymentNotFoundException;
import com.fooddelivery.paymentservice.exception.UnauthorizedException;
import com.fooddelivery.paymentservice.feign.OrderClient;
import com.fooddelivery.paymentservice.models.Payment;
import com.fooddelivery.paymentservice.models.PaymentStatus;
import com.fooddelivery.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * PaymentServiceImpl
 * ------------------
 * This class contains the BUSINESS LOGIC of the payment-service.
 *
 * Responsibilities:
 * - enforce payment rules
 * - validate ownership
 * - interact with order-service (via Feign)
 * - persist payments
 * - publish domain events
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final PaymentEventPublisher eventPublisher;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderClient orderClient,
            PaymentEventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.eventPublisher = eventPublisher;
    }

    /* =========================
       CREATE PAYMENT
       ========================= */
    /**
     * Creates a new payment for an order.
     *
     * Rules enforced:
     * - An order can only have ONE payment
     * - The order must belong to the authenticated user
     * - Payment amount comes ONLY from order-service
     *
     * Initial status = PENDING
     */

    @Override
    public PaymentResponse createPayment(Long orderId, String paymentMethod, Long userId) {

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new PaymentConflictException("Payment already exists for this order");
        }

        OrderClient.OrderSummaryResponse order =
                orderClient.getOrderById(orderId, userId, "CUSTOMER");

        if (!order.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("Order does not belong to user");
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setRestaurantId(order.getRestaurantId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PENDING);

        return toResponse(paymentRepository.save(payment));
    }

    /* =========================
       CONFIRM PAYMENT (CUSTOMER)
       ========================= */

    @Override
    public PaymentResponse confirmPayment(Long paymentId, Long userId) {

        Payment payment = getOwnedPayment(paymentId, userId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentConflictException("Only pending payments can be confirmed");
        }

        payment.setStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);

        // Notify order-service
        orderClient.markOrderAsPaid(
                payment.getOrderId(),
                userId,
                "CUSTOMER"
        );

        // Publish domain event
        eventPublisher.publishPaymentConfirmed(
                new PaymentConfirmedEvent(
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getUserId(),
                        payment.getAmount()
                )
        );

        return toResponse(payment);
    }

    /* =========================
       CANCEL PAYMENT
       ========================= */

    @Override
    public PaymentResponse cancelPayment(Long paymentId, Long userId) {

        Payment payment = getOwnedPayment(paymentId, userId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentConflictException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        return toResponse(payment);
    }

    /* =========================
       REFUND PAYMENT (ADMIN)
       ========================= */

    @Override
    public PaymentResponse refundPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new PaymentConflictException("Only confirmed payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        eventPublisher.publishPaymentRefunded(
                new PaymentRefundedEvent(
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getUserId(),
                        payment.getAmount()
                )
        );

        return toResponse(payment);
    }

    /* =========================
       READ OPERATIONS
       ========================= */

    @Override
    public PaymentResponse getPaymentById(Long paymentId, Long userId) {
        return toResponse(getOwnedPayment(paymentId, userId));
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId, Long userId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }

        return toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> getPaymentsByRestaurant(Long restaurantId) {
        return paymentRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* =========================
       HELPERS
       ========================= */
    /**
     * Fetches a payment and verifies ownership.
     *
     * Centralizes authorization logic.
     */

    private Payment getOwnedPayment(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedException("Payment does not belong to user");
        }
        return payment;
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPaymentMethod()
        );
    }
}
