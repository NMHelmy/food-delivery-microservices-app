package com.fooddelivery.deliveryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";

    // Queues (will be consumed by notification-service)
    public static final String DELIVERY_ASSIGNED_QUEUE = "delivery.assigned.queue";
    public static final String DELIVERY_PICKED_UP_QUEUE = "delivery.picked-up.queue";
    public static final String DELIVERY_DELIVERED_QUEUE = "delivery.delivered.queue";

    // Routing Keys
    public static final String DELIVERY_ASSIGNED_KEY = "delivery.assigned";
    public static final String DELIVERY_PICKED_UP_KEY = "delivery.picked-up";
    public static final String DELIVERY_DELIVERED_KEY = "delivery.delivered";

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

    // Exchange
    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue deliveryAssignedQueue() {
        return new Queue(DELIVERY_ASSIGNED_QUEUE, true);
    }

    @Bean
    public Queue deliveryPickedUpQueue() {
        return new Queue(DELIVERY_PICKED_UP_QUEUE, true);
    }


    @Bean
    public Queue deliveryDeliveredQueue() {
        return new Queue(DELIVERY_DELIVERED_QUEUE, true);
    }

    // Bindings
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
    public Binding deliveryDeliveredBinding() {
        return BindingBuilder.bind(deliveryDeliveredQueue())
                .to(deliveryExchange())
                .with(DELIVERY_DELIVERED_KEY);
    }
}
