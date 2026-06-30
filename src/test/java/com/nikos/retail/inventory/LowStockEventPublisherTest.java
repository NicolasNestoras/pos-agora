package com.nikos.retail.inventory;

import com.nikos.retail.productvariant.ProductVariant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LowStockEventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private LowStockEventPublisher lowStockEventPublisher;

    private VariantStock stock;

    @BeforeEach
    void setUp() {
        ProductVariant variant = new ProductVariant();
        variant.setSku("SKU-1");

        Location warehouse = new Location();
        warehouse.setName("Main Warehouse");

        stock = new VariantStock();
        stock.setProductVariant(variant);
        stock.setLocation(warehouse);
    }

    @Test
    void publishes_whenAvailableAtThreshold() {
        stock.setOnHandQuantity(5); // available = 5 = threshold

        lowStockEventPublisher.checkAndPublish(stock);

        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.LOW_STOCK_ROUTING_KEY), any(LowStockEvent.class));
    }

    @Test
    void doesNotPublish_whenAvailableAboveThreshold() {
        stock.setOnHandQuantity(10);

        lowStockEventPublisher.checkAndPublish(stock);

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }
}