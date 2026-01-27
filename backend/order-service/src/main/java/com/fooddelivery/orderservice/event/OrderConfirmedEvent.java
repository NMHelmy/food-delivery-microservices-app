package com.fooddelivery.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent implements Serializable {
    private Long orderId;
    private Long customerId;
    private String restaurantName;
}
