package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.model.OrderStatus;
import com.fooddelivery.orderservice.model.PaymentStatus;

import java.math.BigDecimal;

public class OrderSummaryResponse {

    private Long orderId;
    private Long userId;
    private BigDecimal total;
    private OrderStatus status;
    private PaymentStatus paymentStatus;

    public OrderSummaryResponse() {}

    public OrderSummaryResponse(
            Long orderId,
            Long userId,
            BigDecimal total,
            OrderStatus status,
            PaymentStatus paymentStatus
    ) {
        this.orderId = orderId;
        this.userId = userId;
        this.total = total;
        this.status = status;
        this.paymentStatus = paymentStatus;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}