package com.fooddelivery.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryArrivingEvent {
    private Long deliveryId;
    private Long orderId;
    private Long customerId;
    private String driverName;
    private Integer estimatedMinutes;
}
