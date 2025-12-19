package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.model.OrderStatus;
import com.fooddelivery.orderservice.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private Long customerId;
    private Long restaurantId;
    private Long deliveryAddressId;
    private List<OrderItemResponseDTO> items;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal tax;
    private BigDecimal total;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String specialInstructions;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}