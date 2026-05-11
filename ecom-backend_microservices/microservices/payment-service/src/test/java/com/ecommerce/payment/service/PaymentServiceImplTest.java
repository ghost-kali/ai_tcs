package com.ecommerce.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.payment.config.PayPalProperties;
import com.ecommerce.payment.dto.PayPalOrderRequest;
import com.ecommerce.payment.dto.PayPalOrderResponse;
import com.ecommerce.payment.repository.PaymentTransactionRepository;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PayPalHttpClient payPalHttpClient;

    @Mock
    private PayPalProperties properties;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @InjectMocks
    private PaymentServiceImpl service;

    @Test
    void createPayPalOrder_usesDefaults_andExtractsApproveUrl() throws Exception {
        // Given: default config values
        when(properties.getCurrency()).thenReturn("USD");
        when(properties.getBrandName()).thenReturn("Brand");
        when(properties.getReturnUrl()).thenReturn("https://return");
        when(properties.getCancelUrl()).thenReturn("https://cancel");

        // And: PayPal Order response includes an "approve" link
        LinkDescription approve = org.mockito.Mockito.mock(LinkDescription.class);
        when(approve.rel()).thenReturn("approve");
        when(approve.href()).thenReturn("https://approve");

        Order order = org.mockito.Mockito.mock(Order.class);
        when(order.id()).thenReturn("ORDER-1");
        when(order.status()).thenReturn("CREATED");
        when(order.links()).thenReturn(List.of(approve));

        @SuppressWarnings("unchecked")
        HttpResponse<Order> response = org.mockito.Mockito.mock(HttpResponse.class);
        when(response.result()).thenReturn(order);

        when(payPalHttpClient.execute(any(com.paypal.orders.OrdersCreateRequest.class))).thenReturn(response);
        when(paymentTransactionRepository.findByProviderOrderId("ORDER-1")).thenReturn(java.util.Optional.empty());
        when(paymentTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PayPalOrderRequest request = new PayPalOrderRequest();
        request.setAmount(new BigDecimal("12.34"));

        // When
        PayPalOrderResponse result = service.createPayPalOrder(request);

        // Then
        assertThat(result.getOrderId()).isEqualTo("ORDER-1");
        assertThat(result.getApproveUrl()).isEqualTo("https://approve");
    }

    @Test
    void createPayPalOrder_whenHttpException4xx_throwsBadRequest() throws Exception {
        // Given
        HttpException ex = org.mockito.Mockito.mock(HttpException.class);
        when(ex.statusCode()).thenReturn(400);
        when(payPalHttpClient.execute(any(com.paypal.orders.OrdersCreateRequest.class))).thenThrow(ex);

        PayPalOrderRequest request = new PayPalOrderRequest();
        request.setAmount(new BigDecimal("1.00"));

        // When + Then
        assertThrows(ResponseStatusException.class, () -> service.createPayPalOrder(request));
    }

    @Test
    void capturePayPalOrder_extractsApproveUrl() throws Exception {
        // Given: PayPal Order response includes an "approve" link
        LinkDescription approve = org.mockito.Mockito.mock(LinkDescription.class);
        when(approve.rel()).thenReturn("approve");
        when(approve.href()).thenReturn("https://approve");

        Order order = org.mockito.Mockito.mock(Order.class);
        when(order.id()).thenReturn("ORDER-2");
        when(order.status()).thenReturn("COMPLETED");
        when(order.links()).thenReturn(List.of(approve));

        @SuppressWarnings("unchecked")
        HttpResponse<Order> response = org.mockito.Mockito.mock(HttpResponse.class);
        when(response.result()).thenReturn(order);

        when(payPalHttpClient.execute(any(com.paypal.orders.OrdersCaptureRequest.class))).thenReturn(response);
        when(paymentTransactionRepository.findByProviderOrderId("ORDER-2")).thenReturn(java.util.Optional.empty());
        when(paymentTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        PayPalOrderResponse result = service.capturePayPalOrder("PAYPAL-ORDER-ID", null);

        // Then
        assertThat(result.getOrderId()).isEqualTo("ORDER-2");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getApproveUrl()).isEqualTo("https://approve");
    }

    @Test
    void capturePayPalOrder_whenHttpException4xx_throwsBadRequest() throws Exception {
        // Given
        HttpException ex = org.mockito.Mockito.mock(HttpException.class);
        when(ex.statusCode()).thenReturn(400);
        when(payPalHttpClient.execute(any(com.paypal.orders.OrdersCaptureRequest.class))).thenThrow(ex);

        // When + Then
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> service.capturePayPalOrder("PAYPAL-ORDER-ID", null));
        assertThat(thrown.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void capturePayPalOrder_whenIOException_throwsBadGateway() throws Exception {
        // Given
        when(payPalHttpClient.execute(any(com.paypal.orders.OrdersCaptureRequest.class)))
                .thenThrow(new IOException("network"));

        // When + Then
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> service.capturePayPalOrder("PAYPAL-ORDER-ID", null));
        assertThat(thrown.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }
}
