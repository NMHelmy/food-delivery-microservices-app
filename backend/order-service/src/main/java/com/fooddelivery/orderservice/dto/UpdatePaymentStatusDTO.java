package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusDTO {

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
}