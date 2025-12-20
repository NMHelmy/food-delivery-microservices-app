package com.fooddelivery.deliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDeliveredEvent implements Serializable {
    private Long deliveryId;
    private Long orderId;
    private Long customerId;
}
