package com.ecommerce.order.controller;

import com.ecommerce.order.dto.MessageResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PageResponse;
import com.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.order.security.JwtAuthenticationToken;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/orders/place")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        JwtAuthenticationToken auth = currentAuth();
        return ResponseEntity.ok(orderService.placeOrder(auth.getUserId(), auth.getEmail(), request));
    }

    @GetMapping("/api/orders")
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(@RequestParam(defaultValue = "0") int pageNumber) {
        return ResponseEntity.ok(orderService.getOrdersForAdmin(pageNumber, 10));
    }

    @GetMapping("/api/admin/orders")
    public ResponseEntity<PageResponse<OrderResponse>> getAdminOrders(@RequestParam(defaultValue = "0") int pageNumber) {
        return ResponseEntity.ok(orderService.getOrdersForAdmin(pageNumber, 10));
    }

    @GetMapping("/api/seller/orders")
    public ResponseEntity<PageResponse<OrderResponse>> getUserOrders(@RequestParam(defaultValue = "0") int pageNumber) {
        return ResponseEntity.ok(orderService.getOrdersForUser(currentAuth().getUserId(), pageNumber, 10));
    }

    @PutMapping("/api/orders/{orderId}/status")
    public ResponseEntity<MessageResponse> updateOrderStatus(@PathVariable Long orderId,
                                                             @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus()));
    }

    @PutMapping("/api/seller/orders/{orderId}/status")
    public ResponseEntity<MessageResponse> updateSellerOrderStatus(@PathVariable Long orderId,
                                                                   @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus()));
    }

    @GetMapping("/api/orders/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("order-service-up");
    }

    private JwtAuthenticationToken currentAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (JwtAuthenticationToken) authentication;
    }
}
