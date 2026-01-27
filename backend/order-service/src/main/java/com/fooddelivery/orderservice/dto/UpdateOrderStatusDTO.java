package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusDTO {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}