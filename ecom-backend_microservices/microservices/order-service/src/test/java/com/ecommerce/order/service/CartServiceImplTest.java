package com.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductSummaryDTO;
import com.ecommerce.order.model.Cart;
import com.ecommerce.order.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartServiceImpl service;

    @Test
    void createOrReplaceUserCart_createsCartAndSavesItems() {
        // Given: user has no existing cart
        when(cartRepository.findByUserId(7L)).thenReturn(Optional.empty());

        // And: product client returns product summary
        ProductSummaryDTO product = new ProductSummaryDTO();
        product.setProductId(1L);
        product.setProductName("Phone");
        product.setPrice(new BigDecimal("100.00"));
        product.setSpecialPrice(new BigDecimal("90.00"));

        when(productClient.getProductById(1L)).thenReturn(product);
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0, Cart.class));

        CartItemRequest itemRequest = new CartItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        // When
        service.createOrReplaceUserCart(7L, List.of(itemRequest));

        // Then: saved cart contains one cart item
        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).hasSize(1);
        assertThat(captor.getValue().getItems().get(0).getProductId()).isEqualTo(1L);
    }
}
