package com.fooddelivery.paymentservice.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PaymentFailedEvent implements Serializable {

    private final Long paymentId;
    private final Long orderId;
    private final Long userId;
    private final String reason;
    private final LocalDateTime occurredAt;

    public PaymentFailedEvent(
            Long paymentId,
            Long orderId,
            Long userId,
            String reason
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
