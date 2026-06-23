package com.nikos.retail.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.nikos.retail.customer.CustomerRepository;
import com.nikos.retail.productvariant.PriceListRepository;
import com.nikos.retail.productvariant.ProductVariantPriceRepository;
import com.nikos.retail.productvariant.ProductVariantRepository;


@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private ProductVariantPriceRepository productVariantPriceRepository;
    @Mock private PriceListRepository priceListRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks 
    private CartService cartService;

    @Test
    void addItem_shouldThrow_whenCartIsCheckedOut(){
        Cart checkedOutCart = new Cart();
        checkedOutCart.setStatus(CartStatus.CHECKED_OUT);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(checkedOutCart));

        CartItemRequest request = new CartItemRequest();
        request.setProductVariantId(1L);
        request.setQuantity(2);

        assertThrows(IllegalStateException.class, () -> cartService.addItem(1L, request));
    }
}
