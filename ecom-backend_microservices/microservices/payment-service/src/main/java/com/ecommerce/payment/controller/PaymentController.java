package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PayPalOrderRequest;
import com.ecommerce.payment.dto.PayPalOrderResponse;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/paypal/order")
    public ResponseEntity<PayPalOrderResponse> createPayPalOrder(@Valid @RequestBody PayPalOrderRequest request) {
        return ResponseEntity.ok(paymentService.createPayPalOrder(request));
    }

    @PostMapping("/paypal/order/{orderId}/capture")
    public ResponseEntity<PayPalOrderResponse> capturePayPalOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.capturePayPalOrder(orderId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("payment-service-up");
    }
}

