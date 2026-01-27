package com.fooddelivery.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmedEvent {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
}
