package com.fooddelivery.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundedEvent {
    private Long paymentId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
}
