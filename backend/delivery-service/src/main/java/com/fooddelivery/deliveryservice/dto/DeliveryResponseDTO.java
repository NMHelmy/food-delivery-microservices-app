package com.fooddelivery.deliveryservice.dto;

import com.fooddelivery.deliveryservice.model.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponseDTO {

    private Long id;
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private Long driverId;
    private Long deliveryAddressId;
    private DeliveryStatus status;
    private String restaurantAddress;
    private String deliveryAddress;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime estimatedDeliveryTime;
    private String deliveryNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
