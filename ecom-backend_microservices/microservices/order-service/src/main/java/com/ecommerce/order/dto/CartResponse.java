package com.ecommerce.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> products;
    private BigDecimal totalPrice;
}
