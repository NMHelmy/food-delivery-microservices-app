package com.fooddelivery.deliveryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long restaurantId;

    private Long driverId; // Null until driver is assigned

    @Column(nullable = false)
    private Long deliveryAddressId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    private String restaurantAddress;
    private String deliveryAddress;

    // Driver location tracking
    private Double driverLatitude;
    private Double driverLongitude;
    private LocalDateTime lastLocationUpdate;

    // Delivery timestamps
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime estimatedDeliveryTime;

    @Column(length = 1000)
    private String deliveryNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
