package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.CartResponse;
import com.ecommerce.order.dto.ProductSummaryDTO;
import com.ecommerce.order.model.Cart;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public void createOrReplaceUserCart(Long userId, List<CartItemRequest> items) {

        System.out.println("Items received: " + items.size()); // ✅ HERE

        Cart cart = cartRepository.findByUserId(userId).orElseGet(Cart::new);
        cart.setUserId(userId);

        cart.getItems().clear();

        for (CartItemRequest request : items) {
            ProductSummaryDTO product = productClient.getProductById(request.getProductId());

            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(product.getProductId());
            item.setProductName(product.getProductName());
            item.setImage(product.getImage());
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice());
            item.setSpecialPrice(product.getSpecialPrice());

            cart.getItems().add(item);

            System.out.println("Added item: " + item.getProductId()); // optional debug
        }

        System.out.println("Cart items after add: " + cart.getItems().size()); // ✅ HERE

        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getUserCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart emptyCart = new Cart();
            emptyCart.setUserId(userId);
            return emptyCart;
        });

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .products(cart.getItems().stream().map(item -> CartItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .image(item.getImage())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .specialPrice(item.getSpecialPrice())
                        .build()).toList())
                .totalPrice(cart.calculateTotal())
                .build();
    }

    @Override
    @Transactional
    public void clearUserCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}
