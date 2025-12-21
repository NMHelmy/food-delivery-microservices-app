package com.fooddelivery.deliveryservice.model;

public enum DeliveryStatus {
    PENDING,           // Delivery created, waiting for driver assignment
    ASSIGNED,          // Driver has been assigned
    PICKED_UP,         // Driver picked up from restaurant
    DELIVERED,         // Successfully delivered
    CANCELLED          // Delivery cancelled
}
