package com.nikos.retail.sale;

import com.nikos.retail.cart.Cart;
import com.nikos.retail.cart.CartItem;
import com.nikos.retail.cart.CartRepository;
import com.nikos.retail.cart.CartStatus;
import com.nikos.retail.inventory.Location;
import com.nikos.retail.inventory.LocationRepository;
import com.nikos.retail.inventory.LocationType;
import com.nikos.retail.inventory.StockAllocationService;
import com.nikos.retail.productvariant.ProductVariant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private CartRepository cartRepository;
    @Mock private StockAllocationService stockAllocationService;
    @Mock private LocationRepository locationRepository;

    @InjectMocks
    private SaleService saleService;

    @Test
    void checkout_shouldThrow_whenCartIsEmpty() {
        Cart emptyCart = new Cart();
        emptyCart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(emptyCart));
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCartId(1L);
        assertThrows(IllegalStateException.class, () -> saleService.checkout(saleRequest));
    }

    @Test
    void checkout_shouldThrow_whenStockInsufficient() {
        ProductVariant variant = new ProductVariant();
        variant.setSku("SKU-1");

        CartItem item = new CartItem();
        item.setProductVariant(variant);
        item.setQuantity(5);
        item.setUnitPrice(BigDecimal.TEN);

        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.getItems().add(item);

        Location store = new Location();
        store.setType(LocationType.STORE);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(locationRepository.findByType(LocationType.STORE)).thenReturn(List.of(store));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new IllegalStateException("Insufficient stock"))
            .when(stockAllocationService).recordSale(any(), any(), anyInt(), any());

        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCartId(1L);
        assertThrows(IllegalStateException.class, () -> saleService.checkout(saleRequest));
    }

    @Test
    void checkout_happyPath_recordsSaleAtStoreForEachItem() {
        ProductVariant variant = new ProductVariant();
        variant.setSku("SKU-1");

        CartItem item = new CartItem();
        item.setProductVariant(variant);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.TEN);

        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.getItems().add(item);

        Location store = new Location();
        store.setType(LocationType.STORE);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(locationRepository.findByType(LocationType.STORE)).thenReturn(List.of(store));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCartId(1L);

        saleService.checkout(saleRequest);


        verify(stockAllocationService).recordSale(eq(variant), eq(store), eq(2), isNull());
        verify(cartRepository).save(argThat(c -> c.getStatus() == CartStatus.CHECKED_OUT));
    }

}