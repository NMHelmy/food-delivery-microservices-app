package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.config.RabbitMQConfig;
import com.fooddelivery.orderservice.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing ORDER_CREATED event for order: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_KEY,
                event
        );
    }

    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Publishing ORDER_CONFIRMED event for order: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CONFIRMED_KEY,
                event
        );
    }

    public void publishOrderReady(OrderReadyEvent event) {
        log.info("Publishing ORDER_READY event for order: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_READY_KEY,
                event
        );
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Publishing ORDER_CANCELLED event for order: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CANCELLED_KEY,
                event
        );
    }
}
