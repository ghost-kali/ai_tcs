package com.ecommerce.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductSummaryDTO {
    private Long productId;
    private String productName;
    private String image;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal specialPrice;
}
