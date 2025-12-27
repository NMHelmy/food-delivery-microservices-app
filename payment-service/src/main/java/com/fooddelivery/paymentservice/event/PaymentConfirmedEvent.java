package com.fooddelivery.paymentservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentConfirmedEvent {
/**
 * Unique identifier of the payment.
 */

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private BigDecimal amount;
    private LocalDateTime occurredAt;

    public PaymentConfirmedEvent() {
    }
    /**
     * Main constructor.
     *
     * Called ONLY when:
     * - Payment has been persisted
     * - Payment status = CONFIRMED
     *
     * This constructor:
     * - Captures a snapshot of the fact
     * - Automatically timestamps the event
     */


    public PaymentConfirmedEvent(
            Long paymentId,
            Long orderId,
            Long userId,
            Long restaurantId,
            BigDecimal amount
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.amount = amount;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
