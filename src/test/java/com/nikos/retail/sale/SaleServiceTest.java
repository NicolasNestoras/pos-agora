package com.nikos.retail.sale;

import com.nikos.retail.cart.Cart;
import com.nikos.retail.cart.CartItem;
import com.nikos.retail.cart.CartRepository;
import com.nikos.retail.cart.CartStatus;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductVariantRepository variantRepository;

    @InjectMocks
    private SaleService saleService;

    @Test
    void checkout_shouldThrow_whenCartIsEmpty() {
        Cart emptyCart = new Cart();
        emptyCart.setStatus(CartStatus.ACTIVE);
        // items defaults to an empty list

        when(cartRepository.findById(1L)).thenReturn(Optional.of(emptyCart));
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCartId(1L);
        assertThrows(IllegalStateException.class, () -> saleService.checkout(saleRequest));
    }

    @Test
    void checkout_shouldThrow_whenStockInsufficient() {
        ProductVariant variant = new ProductVariant();
        variant.setStockQuantity(1); // only 1 in stock

        CartItem item = new CartItem();
        item.setProductVariant(variant);
        item.setQuantity(5); // requesting 5
        item.setUnitPrice(BigDecimal.TEN);

        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.getItems().add(item);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCartId(1L);
        assertThrows(IllegalStateException.class, () -> saleService.checkout(saleRequest));
    }
}