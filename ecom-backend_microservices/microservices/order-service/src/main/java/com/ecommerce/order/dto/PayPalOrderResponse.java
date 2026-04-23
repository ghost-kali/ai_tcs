package com.ecommerce.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayPalOrderResponse {
    private String orderId;
    private String status;
    private String approveUrl;
}
