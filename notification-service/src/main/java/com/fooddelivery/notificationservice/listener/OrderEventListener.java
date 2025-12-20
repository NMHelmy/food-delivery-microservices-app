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
public class OrderEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received ORDER_CREATED event for order: {}", event.getOrderId());

        // Notify customer
        notificationService.createNotification(
                event.getCustomerId(),
                NotificationType.ORDER_CREATED,
                "Order Placed Successfully",
                String.format("Your order #%d has been placed at %s",
                        event.getOrderId(), event.getRestaurantName()),
                event.getOrderId(),
                null,
                null
        );

        // Notify restaurant owner
        notificationService.createNotification(
                event.getRestaurantOwnerId(),
                NotificationType.ORDER_CREATED,
                "New Order Received",
                String.format("New order #%d has been placed", event.getOrderId()),
                event.getOrderId(),
                null,
                null
        );
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CONFIRMED_QUEUE)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received ORDER_CONFIRMED event for order: {}", event.getOrderId());

        notificationService.createNotification(
                event.getCustomerId(),
                NotificationType.ORDER_CONFIRMED,
                "Order Confirmed",
                String.format("%s has confirmed your order #%d and started preparing it",
                        event.getRestaurantName(), event.getOrderId()),
                event.getOrderId(),
                null,
                null
        );
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_READY_QUEUE)
    public void handleOrderReady(OrderReadyEvent event) {
        log.info("Received ORDER_READY event for order: {}", event.getOrderId());

        notificationService.createNotification(
                event.getCustomerId(),
                NotificationType.ORDER_READY,
                "Order Ready for Pickup",
                String.format("Your order #%d is ready and waiting for driver pickup",
                        event.getOrderId()),
                event.getOrderId(),
                null,
                null
        );
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received ORDER_CANCELLED event for order: {}", event.getOrderId());

        notificationService.createNotification(
                event.getCustomerId(),
                NotificationType.ORDER_CANCELLED,
                "Order Cancelled",
                String.format("Your order #%d has been cancelled. Reason: %s",
                        event.getOrderId(), event.getReason()),
                event.getOrderId(),
                null,
                null
        );
    }
}
