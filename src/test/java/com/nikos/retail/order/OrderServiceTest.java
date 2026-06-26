package com.nikos.retail.order;

import com.nikos.retail.cart.CartRepository;
import com.nikos.retail.inventory.LocationRepository;
import com.nikos.retail.inventory.StockAllocationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private StockAllocationService stockAllocationService;
    @Mock private LocationRepository locationRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void updateStatus_shouldThrow_whenSkippingFromPendingToDelivered() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> orderService.updateStatus(1L, OrderStatus.DELIVERED));
    }

    @Test
    void updateStatus_shouldThrow_whenTransitioningFromTerminalState() {
        Order deliveredOrder = new Order();
        deliveredOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

        assertThrows(IllegalStateException.class,
            () -> orderService.updateStatus(1L, OrderStatus.PAID));
    }
}