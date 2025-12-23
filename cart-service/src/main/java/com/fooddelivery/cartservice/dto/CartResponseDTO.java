package com.fooddelivery.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private String restaurantName; // Fetched from restaurant-service
    private List<CartItemResponseDTO> items;
    private BigDecimal subtotal;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
