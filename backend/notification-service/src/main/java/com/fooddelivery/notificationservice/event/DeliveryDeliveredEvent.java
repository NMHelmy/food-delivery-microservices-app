package com.fooddelivery.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDeliveredEvent {
    private Long deliveryId;
    private Long orderId;
    private Long customerId;
}
