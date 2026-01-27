package com.fooddelivery.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {

    private Long id;
    private Long menuItemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
    private String customizations;
}