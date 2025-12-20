package com.fooddelivery.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent implements Serializable {
    private Long paymentId;
    private Long orderId;
    private Long customerId;
    private String reason;
}
