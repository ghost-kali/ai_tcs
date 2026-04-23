package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PayPalOrderRequest;
import com.ecommerce.payment.dto.PayPalOrderResponse;

public interface PaymentService {
    PayPalOrderResponse createPayPalOrder(PayPalOrderRequest request);
    PayPalOrderResponse capturePayPalOrder(String payPalOrderId);
}

