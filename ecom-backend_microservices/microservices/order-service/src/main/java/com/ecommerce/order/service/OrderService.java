package com.ecommerce.order.service;

import com.ecommerce.order.dto.MessageResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PageResponse;

public interface OrderService {
    OrderResponse placeOrder(Long userId, String email, OrderRequest request);
    PageResponse<OrderResponse> getOrdersForAdmin(int pageNumber, int pageSize);
    PageResponse<OrderResponse> getOrdersForUser(Long userId, int pageNumber, int pageSize);
    MessageResponse updateOrderStatus(Long orderId, String status);
}
