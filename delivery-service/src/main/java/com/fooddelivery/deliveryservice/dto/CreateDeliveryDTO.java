package com.fooddelivery.deliveryservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryDTO {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;

    private String restaurantAddress;
    private String deliveryAddress;
    private LocalDateTime estimatedDeliveryTime;
    private String deliveryNotes;
}
