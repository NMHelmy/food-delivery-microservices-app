package com.fooddelivery.paymentservice.dto;

public class PaymentRequest {
    /**
     * ID of the order being paid for.
     *
     * This MUST correspond to:
     * - an existing order
     * - owned by the authenticated user
     *
     * Validation is handled in the service layer.
     */

    private Long orderId;
    private String paymentMethod;

    public PaymentRequest() {
    }

    public PaymentRequest(Long orderId, String paymentMethod) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
