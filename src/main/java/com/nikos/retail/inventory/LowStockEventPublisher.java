package com.nikos.retail.inventory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class LowStockEventPublisher {

    //For now the low stock threshold is arbitrary. I will need to figure out
    //later how to make this number meaningful.
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final RabbitTemplate rabbitTemplate;

    public LowStockEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void checkAndPublish(VariantStock stock) {
        int available = stock.getAvailableQuantity();
        if (available <= LOW_STOCK_THRESHOLD) {
            LowStockEvent event = new LowStockEvent(
                stock.getProductVariant().getId(),
                stock.getProductVariant().getSku(),
                stock.getLocation().getId(),
                stock.getLocation().getName(),
                available
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.LOW_STOCK_ROUTING_KEY, event);
        }
    }
}