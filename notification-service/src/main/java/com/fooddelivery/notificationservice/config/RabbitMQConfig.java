package com.fooddelivery.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";

    // Order Queues
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CONFIRMED_QUEUE = "order.confirmed.queue";
    public static final String ORDER_READY_QUEUE = "order.ready.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";

    // Payment Queues
    public static final String PAYMENT_CONFIRMED_QUEUE = "payment.confirmed.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";
    public static final String PAYMENT_REFUNDED_QUEUE = "payment.refunded.queue";

    // Delivery Queues
    public static final String DELIVERY_ASSIGNED_QUEUE = "delivery.assigned.queue";
    public static final String DELIVERY_PICKED_UP_QUEUE = "delivery.picked-up.queue";
    public static final String DELIVERY_ARRIVING_QUEUE = "delivery.arriving.queue";
    public static final String DELIVERY_DELIVERED_QUEUE = "delivery.delivered.queue";

    // Routing Keys
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String ORDER_CONFIRMED_KEY = "order.confirmed";
    public static final String ORDER_READY_KEY = "order.ready";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String PAYMENT_CONFIRMED_KEY = "payment.confirmed";
    public static final String PAYMENT_FAILED_KEY = "payment.failed";
    public static final String PAYMENT_REFUNDED_KEY = "payment.refunded";
    public static final String DELIVERY_ASSIGNED_KEY = "delivery.assigned";
    public static final String DELIVERY_PICKED_UP_KEY = "delivery.picked-up";
    public static final String DELIVERY_ARRIVING_KEY = "delivery.arriving";
    public static final String DELIVERY_DELIVERED_KEY = "delivery.delivered";

    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ===== EXCHANGES =====
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }

    // ===== ORDER QUEUES =====
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue orderConfirmedQueue() {
        return new Queue(ORDER_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Queue orderReadyQueue() {
        return new Queue(ORDER_READY_QUEUE, true);
    }

    @Bean
    public Queue orderCancelledQueue() {
        return new Queue(ORDER_CANCELLED_QUEUE, true);
    }

    // ===== PAYMENT QUEUES =====
    @Bean
    public Queue paymentConfirmedQueue() {
        return new Queue(PAYMENT_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    public Queue paymentRefundedQueue() {
        return new Queue(PAYMENT_REFUNDED_QUEUE, true);
    }

    // ===== DELIVERY QUEUES =====
    @Bean
    public Queue deliveryAssignedQueue() {
        return new Queue(DELIVERY_ASSIGNED_QUEUE, true);
    }

    @Bean
    public Queue deliveryPickedUpQueue() {
        return new Queue(DELIVERY_PICKED_UP_QUEUE, true);
    }

    @Bean
    public Queue deliveryArrivingQueue() {
        return new Queue(DELIVERY_ARRIVING_QUEUE, true);
    }

    @Bean
    public Queue deliveryDeliveredQueue() {
        return new Queue(DELIVERY_DELIVERED_QUEUE, true);
    }

    // ===== ORDER BINDINGS =====
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_KEY);
    }

    @Bean
    public Binding orderConfirmedBinding() {
        return BindingBuilder.bind(orderConfirmedQueue())
                .to(orderExchange())
                .with(ORDER_CONFIRMED_KEY);
    }

    @Bean
    public Binding orderReadyBinding() {
        return BindingBuilder.bind(orderReadyQueue())
                .to(orderExchange())
                .with(ORDER_READY_KEY);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_KEY);
    }

    // ===== PAYMENT BINDINGS =====
    @Bean
    public Binding paymentConfirmedBinding() {
        return BindingBuilder.bind(paymentConfirmedQueue())
                .to(paymentExchange())
                .with(PAYMENT_CONFIRMED_KEY);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILED_KEY);
    }

    @Bean
    public Binding paymentRefundedBinding() {
        return BindingBuilder.bind(paymentRefundedQueue())
                .to(paymentExchange())
                .with(PAYMENT_REFUNDED_KEY);
    }

    // ===== DELIVERY BINDINGS =====
    @Bean
    public Binding deliveryAssignedBinding() {
        return BindingBuilder.bind(deliveryAssignedQueue())
                .to(deliveryExchange())
                .with(DELIVERY_ASSIGNED_KEY);
    }

    @Bean
    public Binding deliveryPickedUpBinding() {
        return BindingBuilder.bind(deliveryPickedUpQueue())
                .to(deliveryExchange())
                .with(DELIVERY_PICKED_UP_KEY);
    }

    @Bean
    public Binding deliveryArrivingBinding() {
        return BindingBuilder.bind(deliveryArrivingQueue())
                .to(deliveryExchange())
                .with(DELIVERY_ARRIVING_KEY);
    }

    @Bean
    public Binding deliveryDeliveredBinding() {
        return BindingBuilder.bind(deliveryDeliveredQueue())
                .to(deliveryExchange())
                .with(DELIVERY_DELIVERED_KEY);
    }
}
