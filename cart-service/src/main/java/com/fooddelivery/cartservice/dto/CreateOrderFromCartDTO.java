package com.fooddelivery.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderFromCartDTO {
    private Long customerId;
    private Long restaurantId;
    private Long deliveryAddressId;
    private List<CartItemForOrderDTO> items;
    private String specialInstructions;
}
