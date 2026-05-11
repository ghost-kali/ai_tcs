package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UpdatePaymentStatusRequest;

public interface OrderPaymentService {
    OrderResponse updatePayment(Long orderId, UpdatePaymentStatusRequest request);
}

