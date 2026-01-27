package com.fooddelivery.notificationservice.listener;

import com.fooddelivery.notificationservice.config.RabbitMQConfig;
import com.fooddelivery.notificationservice.event.*;
import com.fooddelivery.notificationservice.model.NotificationType;
import com.fooddelivery.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_CONFIRMED_QUEUE)
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("Received PAYMENT_CONFIRMED event for payment: {}", event.getPaymentId());

        notificationService.createNotification(
                event.getUserId(),
                NotificationType.PAYMENT_CONFIRMED,
                "Payment Successful",
                String.format("Your payment of $%.2f for order #%d was successful",
                        event.getAmount(), event.getOrderId()),
                event.getOrderId(),
                event.getPaymentId(),
                null
        );
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PAYMENT_FAILED event for payment: {}", event.getPaymentId());

        notificationService.createNotification(
                event.getUserId(),
                NotificationType.PAYMENT_FAILED,
                "Payment Failed",
                String.format("Payment for order #%d failed. Reason: %s",
                        event.getOrderId(), event.getReason()),
                event.getOrderId(),
                event.getPaymentId(),
                null
        );
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_REFUNDED_QUEUE)
    public void handlePaymentRefunded(PaymentRefundedEvent event) {
        log.info("Received PAYMENT_REFUNDED event for payment: {}", event.getPaymentId());

        notificationService.createNotification(
                event.getUserId(),
                NotificationType.PAYMENT_REFUNDED,
                "Refund Processed",
                String.format("A refund of $%.2f has been processed for order #%d",
                        event.getAmount(), event.getOrderId()),
                event.getOrderId(),
                event.getPaymentId(),
                null
        );
    }
}
