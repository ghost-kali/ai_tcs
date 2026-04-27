package com.ecommerce.payment.service;

import com.ecommerce.payment.config.PayPalProperties;
import com.ecommerce.payment.dto.PayPalOrderRequest;
import com.ecommerce.payment.dto.PayPalOrderResponse;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PayPalHttpClient payPalHttpClient;
    private final PayPalProperties properties;

    @Override
    public PayPalOrderResponse createPayPalOrder(PayPalOrderRequest request) {
        try {
            OrdersCreateRequest createRequest = new OrdersCreateRequest();
            createRequest.prefer("return=representation");

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            String currency = request.getCurrency() == null ? properties.getCurrency() : request.getCurrency();
            String description = request.getDescription() == null ? "Order payment" : request.getDescription();

            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                    .description(description)
                    .amountWithBreakdown(new AmountWithBreakdown()
                            .currencyCode(currency)
                            .value(request.getAmount().toPlainString()));

            ApplicationContext applicationContext = new ApplicationContext()
                    .brandName(properties.getBrandName())
                    .returnUrl(properties.getReturnUrl())
                    .cancelUrl(properties.getCancelUrl())
                    .userAction("PAY_NOW");

            orderRequest.purchaseUnits(List.of(purchaseUnit));
            orderRequest.applicationContext(applicationContext);

            createRequest.requestBody(orderRequest);

            HttpResponse<Order> response = payPalHttpClient.execute(createRequest);
            return mapOrderResponse(response.result());
        } catch (HttpException ex) {
            throw new ResponseStatusException(ex.statusCode() >= 400 && ex.statusCode() < 500
                    ? org.springframework.http.HttpStatus.BAD_REQUEST
                    : org.springframework.http.HttpStatus.BAD_GATEWAY, "PayPal create order failed");
        } catch (IOException ex) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "PayPal create order failed");
        }
    }

    @Override
    public PayPalOrderResponse capturePayPalOrder(String payPalOrderId) {
        try {
            OrdersCaptureRequest captureRequest = new OrdersCaptureRequest(payPalOrderId);
            captureRequest.prefer("return=representation");
            captureRequest.requestBody(new OrderRequest());

            HttpResponse<Order> response = payPalHttpClient.execute(captureRequest);
            return mapOrderResponse(response.result());
        } catch (HttpException ex) {
            throw new ResponseStatusException(ex.statusCode() >= 400 && ex.statusCode() < 500
                    ? org.springframework.http.HttpStatus.BAD_REQUEST
                    : org.springframework.http.HttpStatus.BAD_GATEWAY, "PayPal capture order failed");
        } catch (IOException ex) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "PayPal capture order failed");
        }
    }

    private PayPalOrderResponse mapOrderResponse(Order order) {
        String approveUrl = null;
        List<LinkDescription> links = order.links();
        if (links != null) {
            for (LinkDescription link : links) {
                if (link != null && "approve".equalsIgnoreCase(link.rel())) {
                    approveUrl = link.href();
                    break;
                }
            }
        }

        return PayPalOrderResponse.builder()
                .orderId(order.id())
                .status(order.status())
                .approveUrl(approveUrl)
                .build();
    }
}
