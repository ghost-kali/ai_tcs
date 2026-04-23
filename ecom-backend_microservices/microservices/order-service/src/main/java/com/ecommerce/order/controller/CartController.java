package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartResponse;
import com.ecommerce.order.security.JwtAuthenticationToken;
import com.ecommerce.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/api/cart/create")
    public ResponseEntity<Void> createCart(@Valid @RequestBody List<CartItemRequest> items) {
        cartService.createOrReplaceUserCart(currentUserId(), items);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/carts/users/cart")
    public ResponseEntity<CartResponse> getUserCart() {
        return ResponseEntity.ok(cartService.getUserCart(currentUserId()));
    }

    @DeleteMapping("/api/carts/users/cart")
    public ResponseEntity<Void> clearCart() {
        cartService.clearUserCart(currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/carts/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("cart-service-up");
    }

    private Long currentUserId() {
        return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getUserId();
    }
}
