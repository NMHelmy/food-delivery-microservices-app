package com.fooddelivery.paymentservice.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentConfirmed(PaymentConfirmedEvent event) {
        rabbitTemplate.convertAndSend(
                "payment.exchange",
                "payment.confirmed",
                event
        );
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        rabbitTemplate.convertAndSend(
                "payment.exchange",
                "payment.failed",
                event
        );
    }

    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        rabbitTemplate.convertAndSend(
                "payment.exchange",
                "payment.refunded",
                event
        );
    }
}
