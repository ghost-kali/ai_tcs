package com.ecommerce.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentResponse {
    private Long paymentId;
    private String paymentMethod;
    private String providerName;
    private String paymentStatus;
    private BigDecimal amount;
    private String transactionReference;
}
