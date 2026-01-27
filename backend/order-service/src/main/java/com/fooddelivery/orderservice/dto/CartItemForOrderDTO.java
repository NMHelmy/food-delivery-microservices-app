package com.fooddelivery.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemForOrderDTO {
    private Long menuItemId;
    private Integer quantity;
    private String customizations;
}
