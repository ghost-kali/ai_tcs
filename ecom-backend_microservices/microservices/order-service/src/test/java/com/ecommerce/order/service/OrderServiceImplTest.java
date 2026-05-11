package com.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.CustomerOrder;
import com.ecommerce.order.repository.CustomerOrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @InjectMocks
    private OrderServiceImpl service;

    @Test
    void placeOrder_buildsOrderItemsAndPayment() {
        // Given: an order request with a single item
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(3L);
        item.setProductName("Keyboard");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("25.00"));

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));
        request.setTotalAmount(new BigDecimal("50.00"));

        when(customerOrderRepository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0, CustomerOrder.class));

        // When
        OrderResponse response = service.placeOrder(11L, "u@example.com", request);

        // Then
        assertThat(response.getEmail()).isEqualTo("u@example.com");
        assertThat(response.getOrderItems()).hasSize(1);
        assertThat(response.getPayment()).isNotNull();
        assertThat(response.getPayment().getAmount()).isEqualTo(new BigDecimal("50.00"));
    }
}
