package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartResponse;

import java.util.List;

public interface CartService {
    void createOrReplaceUserCart(Long userId, List<CartItemRequest> items);
    CartResponse getUserCart(Long userId);
    void clearUserCart(Long userId);
}
