package com.fooddelivery.cartservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;

    private String specialInstructions;
}
