package com.fooddelivery.orderservice.model;

public enum OrderStatus {
    PENDING,           // Order created, waiting for restaurant confirmation
    CONFIRMED,         // Restaurant confirmed the order
    PREPARING,         // Restaurant is preparing the order
    READY_FOR_PICKUP,  // Order is ready, waiting for driver
    PICKED_UP,         // Driver picked up the order
    DELIVERED,         // Order delivered to customer
    CANCELLED,         // Order cancelled
    REJECTED           // Restaurant rejected the order
}