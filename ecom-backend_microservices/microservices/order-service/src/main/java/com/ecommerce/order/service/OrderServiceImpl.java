package com.ecommerce.order.service;
import com.ecommerce.order.dto.MessageResponse;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PageResponse;
import com.ecommerce.order.dto.PaymentResponse;
import com.ecommerce.order.model.Address;
import com.ecommerce.order.model.CustomerOrder;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentRecord;
import com.ecommerce.order.model.PaymentStatus;
import com.ecommerce.order.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CustomerOrderRepository customerOrderRepository;


    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, String email, OrderRequest request) {
        CustomerOrder order = new CustomerOrder();
        order.setUserId(userId);
        order.setEmail(email);
        order.setTotalAmount(request.getTotalAmount());
        order.setOrderStatus(OrderStatus.ACCEPTED);
        order.setOrderDate(LocalDateTime.now());

        if (request.getAddressId() != null) {

        }

        request.getItems().forEach(itemRequest -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setImage(itemRequest.getImage());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(itemRequest.getPrice());
            order.getOrderItems().add(item);
        });

        PaymentRecord payment = new PaymentRecord();
        payment.setOrder(order);
        payment.setAmount(request.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod() == null ? "PAYPAL" : request.getPaymentMethod());
        payment.setProviderName("PAYPAL");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setTransactionReference("PENDING");
        order.setPayment(payment);

        return toResponse(customerOrderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersForAdmin(int pageNumber, int pageSize) {
        Page<CustomerOrder> page = customerOrderRepository.findAll(
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "orderDate")));
        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersForUser(Long userId, int pageNumber, int pageSize) {
        Page<CustomerOrder> page = customerOrderRepository.findByUserIdOrderByOrderDateDesc(
                userId,
                PageRequest.of(pageNumber, pageSize));
        return toPageResponse(page);
    }

    @Override
    @Transactional
    public MessageResponse updateOrderStatus(Long orderId, String status) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        order.setOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
        customerOrderRepository.save(order);
        return new MessageResponse("Order updated successfully");
    }

    private PageResponse<OrderResponse> toPageResponse(Page<CustomerOrder> page) {
        return PageResponse.<OrderResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .email(order.getEmail())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .addressId(order.getAddress() != null ? order.getAddress().getAddressId() : null)
                .orderItems(order.getOrderItems().stream().map(item -> OrderItemResponse.builder()
                        .orderItemId(item.getOrderItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .image(item.getImage())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).toList())
                .payment(order.getPayment() == null ? null : PaymentResponse.builder()
                        .paymentId(order.getPayment().getPaymentId())
                        .paymentMethod(order.getPayment().getPaymentMethod())
                        .providerName(order.getPayment().getProviderName())
                        .paymentStatus(order.getPayment().getPaymentStatus().name())
                        .amount(order.getPayment().getAmount())
                        .transactionReference(order.getPayment().getTransactionReference())
                        .build())
                .build();
    }
}
