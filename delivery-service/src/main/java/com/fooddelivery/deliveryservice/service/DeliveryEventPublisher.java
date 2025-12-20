package com.fooddelivery.deliveryservice.service;

import com.fooddelivery.deliveryservice.config.RabbitMQConfig;
import com.fooddelivery.deliveryservice.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDeliveryAssigned(DeliveryAssignedEvent event) {
        log.info("Publishing DELIVERY_ASSIGNED event for delivery: {}", event.getDeliveryId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELIVERY_EXCHANGE,
                RabbitMQConfig.DELIVERY_ASSIGNED_KEY,
                event
        );
    }

    public void publishDeliveryPickedUp(DeliveryPickedUpEvent event) {
        log.info("Publishing DELIVERY_PICKED_UP event for delivery: {}", event.getDeliveryId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELIVERY_EXCHANGE,
                RabbitMQConfig.DELIVERY_PICKED_UP_KEY,
                event
        );
    }


    public void publishDeliveryDelivered(DeliveryDeliveredEvent event) {
        log.info("Publishing DELIVERY_DELIVERED event for delivery: {}", event.getDeliveryId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELIVERY_EXCHANGE,
                RabbitMQConfig.DELIVERY_DELIVERED_KEY,
                event
        );
    }
}
