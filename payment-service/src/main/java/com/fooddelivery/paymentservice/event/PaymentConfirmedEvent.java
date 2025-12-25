package com.fooddelivery.paymentservice.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentConfirmedEvent implements Serializable {

    private final Long paymentId;
    private final Long orderId;
    private final Long userId;
    private final Long restaurantId;
    private final BigDecimal amount;
    private final LocalDateTime occurredAt;

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

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
