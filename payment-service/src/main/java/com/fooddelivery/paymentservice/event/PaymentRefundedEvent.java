package com.fooddelivery.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundedEvent implements Serializable {
    private Long paymentId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
}
