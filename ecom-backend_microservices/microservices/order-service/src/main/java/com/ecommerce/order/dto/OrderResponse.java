package com.ecommerce.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private String email;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime orderDate;
    private PaymentResponse payment;
    private BigDecimal totalAmount;
    private String orderStatus;
    private Long addressId;
}
