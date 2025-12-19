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
public class DeliveryEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_ASSIGNED_QUEUE)
    public void handleDeliveryAssigned(DeliveryAssignedEvent event) {
        log.info("Received DELIVERY_ASSIGNED event for delivery: {}", event.getDeliveryId());

        // Notify customer
        notificationService.createNotification(
                event.getCustomerId(),
                NotificationType.DELIVERY_ASSIGNED,
                "Driver Assigned",
                String.format("Driver %s has been assigned to deliver your order #%d",
                        event.getDriverName(), event.getOrderId()),
                event.getOrderId(),
                null,
                event.getDeliveryId()
        );

        // Notify driver
        notificationService.createNotification(
                event.getDriverId(),
                NotificationType.DELIVERY_ASSIGNED,
                "New Delivery Assignment",
                String.format("You have been assigned to deliver order #%d",
                        event.getOrderId()),
                event.getOrderId(),
                null,
                event.getDeliveryId()
        );
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_PICKED_UP_QUEUE)
    public void handleDeliveryPickedUp(DeliveryPickedUpEvent event) {
        log.info("Received DELIVERY_PICKED_UP event for delivery: {}", event.getDeliveryId());

        notificationService.createNotification(
                event.getCustomerId(),
                NotificationType.DELIVERY_PICKED_UP,
                "Order Picked Up",
                String.format("Driver %s has picked up your order #%d and is on the way",
                        event.getDriverName(), event.getOrderId()),
                event.getOrderId(),
                null,
                event.getDeliveryId()
        );
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_ARRIVING_QUEUE)
    public void handleDeliveryArriving(DeliveryArrivingEvent event) {
        log.info("Received DELIVERY_ARRIVING event for delivery: {}", event.getDeliveryId());

        notificationService.createNotification(
                event.getCustomerId(),
                NotificationType.DELIVERY_ARRIVING,
                "Driver Arriving Soon",
                String.format("Driver %s will arrive in approximately %d minutes",
                        event.getDriverName(), event.getEstimatedMinutes()),
                event.getOrderId(),
                null,
                event.getDeliveryId()
        );
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_DELIVERED_QUEUE)
    public void handleDeliveryDelivered(DeliveryDeliveredEvent event) {
        log.info("Received DELIVERY_DELIVERED event for delivery: {}", event.getDeliveryId());

        notificationService.createNotification(
                event.getCustomerId(),
                NotificationType.DELIVERY_DELIVERED,
                "Order Delivered",
                String.format("Your order #%d has been successfully delivered. Enjoy your meal!",
                        event.getOrderId()),
                event.getOrderId(),
                null,
                event.getDeliveryId()
        );
    }
}
