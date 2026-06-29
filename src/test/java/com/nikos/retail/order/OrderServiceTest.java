package com.nikos.retail.order;

import com.nikos.retail.cart.Cart;
import com.nikos.retail.cart.CartItem;
import com.nikos.retail.cart.CartRepository;
import com.nikos.retail.cart.CartStatus;
import com.nikos.retail.customer.Customer;
import com.nikos.retail.customer.CustomerType;
import com.nikos.retail.inventory.AllocationResult;
import com.nikos.retail.inventory.Location;
import com.nikos.retail.inventory.LocationRepository;
import com.nikos.retail.inventory.LocationType;
import com.nikos.retail.inventory.StockAllocationService;
import com.nikos.retail.productvariant.ProductVariant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private StockAllocationService stockAllocationService;
    @Mock private LocationRepository locationRepository;

    @InjectMocks
    private OrderService orderService;

    // ---- updateStatus() ----

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

    @Test
    void updateStatus_toShipped_commitsReservations_neverReleases() {
        Customer customer = new Customer();
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateStatus(1L, OrderStatus.SHIPPED);

        verify(stockAllocationService).commitReservations(order);
        verify(stockAllocationService, never()).releaseReservations(any());
    }

    @Test
    void updateStatus_toCancelled_releasesReservations_neverCommits() {
        Customer customer = new Customer();
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateStatus(1L, OrderStatus.CANCELLED);

        verify(stockAllocationService).releaseReservations(order);
        verify(stockAllocationService, never()).commitReservations(any());
    }


    @Test
    void placeOrder_throws_whenCartHasNoCustomer() {
        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        OrderRequest request = new OrderRequest();
        request.setCartId(1L);

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void placeOrder_throws_whenCartAlreadyCheckedOut() {
        Cart cart = new Cart();
        cart.setCustomer(new Customer());
        cart.setStatus(CartStatus.CHECKED_OUT);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        OrderRequest request = new OrderRequest();
        request.setCartId(1L);

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void placeOrder_throws_whenCartEmpty() {
        Cart cart = new Cart();
        cart.setCustomer(new Customer());
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        OrderRequest request = new OrderRequest();
        request.setCartId(1L);

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void placeOrder_throws_whenNoWarehouseConfigured() {
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.RETAIL);

        CartItem item = new CartItem();
        item.setProductVariant(new ProductVariant());
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.TEN);

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.ACTIVE);
        cart.getItems().add(item);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(locationRepository.findByType(LocationType.WAREHOUSE)).thenReturn(List.of());

        OrderRequest request = new OrderRequest();
        request.setCartId(1L);

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(request));
    }


    @Test
    void placeOrder_retailCustomer_allocatesWithBackorderDisallowed() {
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.RETAIL);

        ProductVariant variant = new ProductVariant();
        variant.setSku("SKU-1");

        CartItem item = new CartItem();
        item.setProductVariant(variant);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.TEN);

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.ACTIVE);
        cart.getItems().add(item);

        Location warehouse = new Location();
        warehouse.setType(LocationType.WAREHOUSE);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(locationRepository.findByType(LocationType.WAREHOUSE)).thenReturn(List.of(warehouse));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockAllocationService.allocate(any(), any(), anyInt(), anyBoolean(), any()))
            .thenReturn(new AllocationResult(2, 0, false));

        OrderRequest request = new OrderRequest();
        request.setCartId(1L);
        request.setShippingAddress("123 Main St");

        orderService.placeOrder(request);

        ArgumentCaptor<Boolean> allowBackorderCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(stockAllocationService)
            .allocate(eq(variant), eq(warehouse), eq(2), allowBackorderCaptor.capture(), any());
        assertFalse(allowBackorderCaptor.getValue());
    }

    @Test
    void placeOrder_wholesaleCustomer_allocatesWithBackorderAllowed() {
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.WHOLESALE);

        ProductVariant variant = new ProductVariant();
        variant.setSku("SKU-2");

        CartItem item = new CartItem();
        item.setProductVariant(variant);
        item.setQuantity(10);
        item.setUnitPrice(BigDecimal.TEN);

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.ACTIVE);
        cart.getItems().add(item);

        Location warehouse = new Location();
        warehouse.setType(LocationType.WAREHOUSE);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(locationRepository.findByType(LocationType.WAREHOUSE)).thenReturn(List.of(warehouse));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockAllocationService.allocate(any(), any(), anyInt(), anyBoolean(), any()))
            .thenReturn(new AllocationResult(4, 6, false));

        OrderRequest request = new OrderRequest();
        request.setCartId(1L);
        request.setShippingAddress("123 Main St");

        orderService.placeOrder(request);

        ArgumentCaptor<Boolean> allowBackorderCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(stockAllocationService)
            .allocate(eq(variant), eq(warehouse), eq(10), allowBackorderCaptor.capture(), any());
        assertTrue(allowBackorderCaptor.getValue());
    }
}