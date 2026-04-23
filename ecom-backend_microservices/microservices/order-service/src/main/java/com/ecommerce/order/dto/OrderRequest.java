package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;

    @NotNull
    private BigDecimal totalAmount;

    private Long addressId;

    private String paymentMethod;
}
