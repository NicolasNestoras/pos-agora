package com.nikos.retail.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LowStockEventListener {

    private static final Logger log = LoggerFactory.getLogger(LowStockEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.LOW_STOCK_QUEUE)
    public void handleLowStock(LowStockEvent event) {

        log.warn("LOW STOCK ALERT: SKU {} at {} has only {} units available (variantId={}, locationId={})",
            event.sku(), event.locationName(), event.availableQuantity(), event.variantId(), event.locationId());
    }
}