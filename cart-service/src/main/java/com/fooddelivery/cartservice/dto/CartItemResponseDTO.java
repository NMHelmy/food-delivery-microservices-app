package com.fooddelivery.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal itemTotal; // price * quantity
    private String customizations;
}
