package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {
    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    private String image;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private BigDecimal price;
}
