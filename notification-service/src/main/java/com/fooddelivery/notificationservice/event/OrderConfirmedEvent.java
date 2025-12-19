package com.fooddelivery.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {
    private Long orderId;
    private Long customerId;
    private String restaurantName;
}
