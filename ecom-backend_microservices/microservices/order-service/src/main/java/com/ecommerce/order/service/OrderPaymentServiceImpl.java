package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PaymentResponse;
import com.ecommerce.order.dto.UpdatePaymentStatusRequest;
import com.ecommerce.order.model.CustomerOrder;
import com.ecommerce.order.model.PaymentRecord;
import com.ecommerce.order.model.PaymentStatus;
import com.ecommerce.order.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final CustomerOrderRepository customerOrderRepository;

    @Override
    @Transactional
    public OrderResponse updatePayment(Long orderId, UpdatePaymentStatusRequest request) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getPayment() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment record not found for this order");
        }
        if (request == null || request.getTransactionReference() == null || request.getTransactionReference().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionReference is required");
        }

        PaymentStatus nextStatus = PaymentStatus.COMPLETED;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                nextStatus = PaymentStatus.valueOf(request.getStatus().trim().toUpperCase(Locale.ROOT));
            } catch (Exception ignored) {
                nextStatus = PaymentStatus.COMPLETED;
            }
        }

        PaymentRecord payment = order.getPayment();
        payment.setPaymentStatus(nextStatus);
        payment.setTransactionReference(request.getTransactionReference().trim());

        CustomerOrder saved = customerOrderRepository.save(order);
        return toResponse(saved);
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

