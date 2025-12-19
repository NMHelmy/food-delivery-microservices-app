package com.fooddelivery.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignedEvent {
    private Long deliveryId;
    private Long orderId;
    private Long customerId;
    private Long driverId;
    private String driverName;
}
