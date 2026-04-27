package com.ecommerce.payment.service;

import com.ecommerce.payment.config.PayPalProperties;
import com.ecommerce.payment.dto.PayPalOrderRequest;
import com.ecommerce.payment.dto.PayPalOrderResponse;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import com.paypal.http.Headers;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PayPalHttpClient payPalHttpClient;

    @Mock
    private HttpResponse<Order> httpResponse;

    @Captor
    private ArgumentCaptor<HttpRequest<Order>> httpRequestCaptor;

    private PayPalProperties properties;
    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new PayPalProperties();
        properties.setBrandName("Test Store");
        properties.setCurrency("USD");
        properties.setReturnUrl("http://localhost/return");
        properties.setCancelUrl("http://localhost/cancel");
        service = new PaymentServiceImpl(payPalHttpClient, properties);
    }

    @Test
    void createPayPalOrder_buildsV2OrderRequestAndMapsApproveUrl() throws Exception {
        Order order = new Order()
                .id("ORDER-123")
                .status("CREATED")
                .links(List.of(
                        new LinkDescription().rel("self").href("http://api/orders/ORDER-123"),
                        new LinkDescription().rel("approve").href("http://approve/ORDER-123")
                ));

        when(httpResponse.result()).thenReturn(order);
        when(payPalHttpClient.execute(any(HttpRequest.class))).thenReturn((HttpResponse) httpResponse);

        PayPalOrderRequest request = new PayPalOrderRequest();
        request.setAmount(new BigDecimal("12.34"));
        request.setDescription("My order");

        PayPalOrderResponse response = service.createPayPalOrder(request);

        assertEquals("ORDER-123", response.getOrderId());
        assertEquals("CREATED", response.getStatus());
        assertEquals("http://approve/ORDER-123", response.getApproveUrl());

        verify(payPalHttpClient).execute(httpRequestCaptor.capture());
        HttpRequest<Order> httpRequest = httpRequestCaptor.getValue();
        assertEquals("POST", httpRequest.verb());
        assertTrue(httpRequest.path().contains("v2/checkout/orders"));
        assertEquals("return=representation", httpRequest.headers().header("Prefer"));

        Object body = httpRequest.requestBody();
        assertNotNull(body);
        assertInstanceOf(OrderRequest.class, body);

        OrderRequest orderRequest = (OrderRequest) body;
        assertEquals("CAPTURE", orderRequest.checkoutPaymentIntent());
        assertNotNull(orderRequest.applicationContext());
        assertEquals("PAY_NOW", orderRequest.applicationContext().userAction());
        assertEquals("Test Store", orderRequest.applicationContext().brandName());
        assertEquals("http://localhost/return", orderRequest.applicationContext().returnUrl());
        assertEquals("http://localhost/cancel", orderRequest.applicationContext().cancelUrl());

        assertNotNull(orderRequest.purchaseUnits());
        assertEquals(1, orderRequest.purchaseUnits().size());
        assertEquals("My order", orderRequest.purchaseUnits().get(0).description());
        assertNotNull(orderRequest.purchaseUnits().get(0).amountWithBreakdown());
        assertEquals("USD", orderRequest.purchaseUnits().get(0).amountWithBreakdown().currencyCode());
        assertEquals("12.34", orderRequest.purchaseUnits().get(0).amountWithBreakdown().value());
    }

    @Test
    void capturePayPalOrder_callsV2CaptureEndpointAndMapsApproveUrl() throws Exception {
        Order order = new Order()
                .id("ORDER-999")
                .status("COMPLETED")
                .links(List.of(new LinkDescription().rel("approve").href("http://approve/ORDER-999")));

        when(httpResponse.result()).thenReturn(order);
        when(payPalHttpClient.execute(any(HttpRequest.class))).thenReturn((HttpResponse) httpResponse);

        PayPalOrderResponse response = service.capturePayPalOrder("ORDER-999");

        assertEquals("ORDER-999", response.getOrderId());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("http://approve/ORDER-999", response.getApproveUrl());

        verify(payPalHttpClient).execute(httpRequestCaptor.capture());
        HttpRequest<Order> httpRequest = httpRequestCaptor.getValue();
        assertEquals("POST", httpRequest.verb());
        assertTrue(httpRequest.path().contains("v2/checkout/orders/ORDER-999/capture"));
        assertEquals("return=representation", httpRequest.headers().header("Prefer"));
        assertNotNull(httpRequest.requestBody());
        assertInstanceOf(OrderRequest.class, httpRequest.requestBody());
    }

    @Test
    void createPayPalOrder_maps4xxHttpExceptionToBadRequest() throws Exception {
        when(payPalHttpClient.execute(any())).thenThrow(new HttpException("bad request", 400, new Headers()));

        PayPalOrderRequest request = new PayPalOrderRequest();
        request.setAmount(new BigDecimal("1.00"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createPayPalOrder(request));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void capturePayPalOrder_maps5xxHttpExceptionToBadGateway() throws Exception {
        when(payPalHttpClient.execute(any())).thenThrow(new HttpException("server error", 500, new Headers()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.capturePayPalOrder("X"));
        assertEquals(502, ex.getStatusCode().value());
    }

    @Test
    void capturePayPalOrder_mapsIoExceptionToBadGateway() throws Exception {
        when(payPalHttpClient.execute(any())).thenThrow(new IOException("network"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.capturePayPalOrder("X"));
        assertEquals(502, ex.getStatusCode().value());
    }
}
