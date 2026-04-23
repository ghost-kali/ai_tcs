package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    ProductSummaryDTO getProductById(@PathVariable("productId") Long productId);
}
