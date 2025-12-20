package com.fooddelivery.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private Long restaurantOwnerId;
    private String restaurantName;
}
