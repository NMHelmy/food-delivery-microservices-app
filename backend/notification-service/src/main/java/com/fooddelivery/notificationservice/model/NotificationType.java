package com.fooddelivery.notificationservice.model;

public enum NotificationType {
    // Order Events
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_READY,
    ORDER_CANCELLED,

    // Payment Events
    PAYMENT_CONFIRMED,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,

    // Delivery Events
    DELIVERY_ASSIGNED,
    DELIVERY_PICKED_UP,
    DELIVERY_DELIVERED
}
