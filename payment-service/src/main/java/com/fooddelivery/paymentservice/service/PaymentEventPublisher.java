package com.fooddelivery.paymentservice.service;

import com.fooddelivery.paymentservice.config.RabbitMQConfig;
import com.fooddelivery.paymentservice.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("Publishing PAYMENT_CONFIRMED event for payment: {}", event.getPaymentId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_CONFIRMED_KEY,
                event
        );
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PAYMENT_FAILED event for payment: {}", event.getPaymentId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_FAILED_KEY,
                event
        );
    }

    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        log.info("Publishing PAYMENT_REFUNDED event for payment: {}", event.getPaymentId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_REFUNDED_KEY,
                event
        );
    }
}
