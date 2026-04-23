package com.ecommerce.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayPalOrderRequest {
    @NotNull
    private BigDecimal amount;
    private String currency;
    private String description;
}

