package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PayPalOrderRequest;
import com.ecommerce.payment.dto.PayPalOrderResponse;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/paypal/order")
    public ResponseEntity<PayPalOrderResponse> createPayPalOrder(
            @Valid @RequestBody PayPalOrderRequest request
    ) {

        log.info(
                "Create PayPal order request received. Amount: {}, Currency: {}",
                request.getAmount(),
                request.getCurrency()
        );

        PayPalOrderResponse response =
                paymentService.createPayPalOrder(request);

        log.info(
                "PayPal order created successfully. OrderId: {}, Status: {}",
                response.getOrderId(),
                response.getStatus()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/paypal/order/{orderId}/capture")
    public ResponseEntity<PayPalOrderResponse> capturePayPalOrder(
            @PathVariable String orderId,
            @RequestParam(name = "appOrderId", required = false)
            Long appOrderId
    ) {

        log.info(
                "Capture PayPal order request received. PayPalOrderId: {}, AppOrderId: {}",
                orderId,
                appOrderId
        );

        PayPalOrderResponse response =
                paymentService.capturePayPalOrder(orderId, appOrderId);

        log.info(
                "PayPal order captured successfully. OrderId: {}, Status: {}",
                response.getOrderId(),
                response.getStatus()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {

        log.info("Health check endpoint called");

        return ResponseEntity.ok("payment-service-up");
    }
}