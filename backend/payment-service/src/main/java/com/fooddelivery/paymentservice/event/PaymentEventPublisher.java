package com.fooddelivery.paymentservice.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
/**
 * PaymentEventPublisher
 * --------------------
 * This class is responsible for PUBLISHING domain events to RabbitMQ.
 *
 * Key idea:
 * - payment-service decides WHAT happened
 * - this class decides HOW that fact is broadcast to the system
 */
@Component
public class PaymentEventPublisher {

    private static final String PAYMENT_EXCHANGE = "payment.exchange";

    private static final String PAYMENT_CONFIRMED_ROUTING_KEY = "payment.confirmed";
    private static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    private static final String PAYMENT_REFUNDED_ROUTING_KEY = "payment.refunded";
    /**foreasy testing*/

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Must be called ONLY AFTER payment is persisted and confirmed.
     */
    public void publishPaymentConfirmed(PaymentConfirmedEvent event) {
        rabbitTemplate.convertAndSend(
                PAYMENT_EXCHANGE,
                PAYMENT_CONFIRMED_ROUTING_KEY,
                event
        );
    }

    /**
     * Must be called ONLY AFTER payment is persisted as FAILED.
     */
    public void publishPaymentFailed(PaymentFailedEvent event) {
        rabbitTemplate.convertAndSend(
                PAYMENT_EXCHANGE,
                PAYMENT_FAILED_ROUTING_KEY,
                event
        );
    }

    /**
     * Must be called ONLY AFTER payment is persisted as REFUNDED.
     */
    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        rabbitTemplate.convertAndSend(
                PAYMENT_EXCHANGE,
                PAYMENT_REFUNDED_ROUTING_KEY,
                event
        );
    }
}
