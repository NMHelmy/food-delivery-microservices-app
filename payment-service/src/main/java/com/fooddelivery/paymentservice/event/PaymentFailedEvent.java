package com.fooddelivery.paymentservice.event;

import java.time.LocalDateTime;

public class PaymentFailedEvent {

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private String reason;
    private LocalDateTime occurredAt;

    public PaymentFailedEvent() {
    }

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

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
