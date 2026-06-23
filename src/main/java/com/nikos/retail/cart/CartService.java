package com.nikos.retail.cart;

import org.springframework.stereotype.Service;

import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.customer.Customer;
import com.nikos.retail.customer.CustomerRepository;
import com.nikos.retail.customer.CustomerType;
import com.nikos.retail.productvariant.PriceListRepository;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantPrice;
import com.nikos.retail.productvariant.ProductVariantPriceRepository;
import com.nikos.retail.productvariant.ProductVariantRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CustomerRepository customerRepository;
    private final PriceListRepository priceListRepository;
    private final ProductVariantPriceRepository productVariantPriceRepository;

    public CartService(CartRepository cartRepository,CartItemRepository cartItemRepository,ProductVariantRepository productVariantRepository,CustomerRepository customerRepository, PriceListRepository priceListRepository, ProductVariantPriceRepository productVariantPriceRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.customerRepository = customerRepository;
        this.priceListRepository = priceListRepository;
        this.productVariantPriceRepository = productVariantPriceRepository;
    }

    public CartResponse createCart(Long customerId) {
        Cart cart = new Cart();

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
            cart.setCustomer(customer);
        }

        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public CartResponse getCartById(Long id) {
        Cart cart = findCartOrThrow(id);
        return CartResponse.fromEntity(cart);
    }

    public CartResponse addItem(Long cartId, CartItemRequest request) {
        Cart cart = findCartOrThrow(cartId);

        if (cart.getStatus() == CartStatus.CHECKED_OUT) {
            throw new IllegalStateException("Cannot add items to a checked out cart");
        }

        ProductVariant productVariant = productVariantRepository.findById(request.getProductVariantId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product variant not found with id: " + request.getProductVariantId()));

        BigDecimal price = resolvePrice(cart,productVariant);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductVariant(productVariant);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(price); 

        cartItemRepository.save(item);

        Cart updated = findCartOrThrow(cartId);
        return CartResponse.fromEntity(updated);
    }

    private BigDecimal resolvePrice(Cart cart, ProductVariant productVariant) {
        if (cart.getCustomer() == null || cart.getCustomer().getCustomerType() == CustomerType.RETAIL) {
            return productVariant.getPrice(); // base price for retail / anonymous
        }

        String priceListName = cart.getCustomer().getCustomerType().name(); // "WHOLESALE"
        return priceListRepository.findByName(priceListName)
            .flatMap(priceList -> productVariantPriceRepository.findByProductVariantIdAndPriceListId(
                productVariant.getId(), priceList.getId()))
            .map(ProductVariantPrice::getPrice)
            .orElse(productVariant.getPrice()); // fallback to base price if no override exists
    }

    public CartResponse holdCart(Long cartId) {
        Cart cart = findCartOrThrow(cartId);
        cart.setStatus(CartStatus.HELD);
        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public CartResponse resumeCart(Long cartId) {
        Cart cart = findCartOrThrow(cartId);
        cart.setStatus(CartStatus.ACTIVE);
        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public List<CartResponse> getHeldCarts() {
        return cartRepository.findByStatus(CartStatus.HELD)
            .stream()
            .map(CartResponse::fromEntity)
            .collect(Collectors.toList());
    }

    private Cart findCartOrThrow(Long id) {
        return cartRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));
    }
}