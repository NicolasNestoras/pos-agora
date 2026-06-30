package com.nikos.retail.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "inventory.exchange";
    public static final String LOW_STOCK_QUEUE = "inventory.low-stock.queue";
    public static final String LOW_STOCK_ROUTING_KEY = "inventory.lowstock";

    @Bean
    public TopicExchange inventoryExchange(){
        return new TopicExchange(EXCHANGE);
    }

    @Bean 
    public Queue lowStockQueue(){
        return new Queue(LOW_STOCK_QUEUE, true);
    }


    @Bean
    public Binding lowStockBinding(Queue lowStockQueue, TopicExchange inventoryExchange){
        return BindingBuilder
        .bind(lowStockQueue)
        .to(inventoryExchange)
        .with(LOW_STOCK_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}