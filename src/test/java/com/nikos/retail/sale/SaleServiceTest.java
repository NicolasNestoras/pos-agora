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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

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
        // empty-cart check happens before the location lookup, so
        // locationRepository doesn't need stubbing for this test

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
        // Sale is saved before stock is checked — hand back the same
        // entity, items already attached, so the loop afterward works.
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // The real insufficient-stock check now lives inside
        // StockAllocationService — simulate it throwing, same as it
        // would for real with 5 requested against too little stock.
        doThrow(new IllegalStateException("Insufficient stock"))
            .when(stockAllocationService).recordSale(any(), any(), anyInt(), any());

        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCartId(1L);
        assertThrows(IllegalStateException.class, () -> saleService.checkout(saleRequest));
    }
}