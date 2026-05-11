package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UpdatePaymentStatusRequest;
import com.ecommerce.order.service.OrderPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/orders")
@RequiredArgsConstructor
public class InternalOrderPaymentController {

    private final OrderPaymentService orderPaymentService;

    @Value("${internal.token:dev-internal-token}")
    private String internalToken;

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<OrderResponse> updatePayment(@RequestHeader(value = "X-Internal-Token", required = false) String token,
                                                      @PathVariable Long orderId,
                                                      @Valid @RequestBody UpdatePaymentStatusRequest request) {
        if (token == null || token.isBlank() || !token.equals(internalToken)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(orderPaymentService.updatePayment(orderId, request));
    }
}

