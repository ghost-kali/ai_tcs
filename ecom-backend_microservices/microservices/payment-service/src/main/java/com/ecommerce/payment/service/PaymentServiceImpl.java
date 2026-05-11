package com.ecommerce.payment.service;

import com.ecommerce.payment.config.PayPalProperties;
import com.ecommerce.payment.dto.PayPalOrderRequest;
import com.ecommerce.payment.dto.PayPalOrderResponse;
import com.ecommerce.payment.model.PaymentProvider;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.model.PaymentTransaction;
import com.ecommerce.payment.repository.PaymentTransactionRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PayPalHttpClient payPalHttpClient;
    private final PayPalProperties properties;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public PayPalOrderResponse createPayPalOrder(PayPalOrderRequest request) {

        try {

            log.info(
                    "Creating PayPal order. Amount={}, Currency={}",
                    request.getAmount(),
                    request.getCurrency()
            );

            OrdersCreateRequest createRequest =
                    new OrdersCreateRequest();

            createRequest.prefer("return=representation");

            OrderRequest orderRequest = new OrderRequest();

            orderRequest.checkoutPaymentIntent("CAPTURE");

            String currency =
                    request.getCurrency() == null
                            ? properties.getCurrency()
                            : request.getCurrency();

            String description =
                    request.getDescription() == null
                            ? "Order payment"
                            : request.getDescription();

            PurchaseUnitRequest purchaseUnit =
                    new PurchaseUnitRequest()
                            .description(description)
                            .amountWithBreakdown(
                                    new AmountWithBreakdown()
                                            .currencyCode(currency)
                                            .value(
                                                    request.getAmount()
                                                            .toPlainString()
                                            )
                            );

            ApplicationContext applicationContext =
                    new ApplicationContext()
                            .brandName(properties.getBrandName())
                            .returnUrl(properties.getReturnUrl())
                            .cancelUrl(properties.getCancelUrl())
                            .userAction("PAY_NOW");

            orderRequest.purchaseUnits(List.of(purchaseUnit));

            orderRequest.applicationContext(applicationContext);

            createRequest.requestBody(orderRequest);

            log.info("Calling PayPal create order API");

            HttpResponse<Order> response =
                    payPalHttpClient.execute(createRequest);

            log.info(
                    "PayPal create order success. HTTP Status={}",
                    response.statusCode()
            );

            PayPalOrderResponse out =
                    mapOrderResponse(response.result());

            log.info(
                    "PayPal order created successfully. OrderId={}, Status={}",
                    out.getOrderId(),
                    out.getStatus()
            );

            persistCreatedTransaction(request, out);

            log.info(
                    "Payment transaction saved in database. OrderId={}",
                    out.getOrderId()
            );

            return out;

        } catch (HttpException ex) {

            log.error(
                    "PayPal create order failed. StatusCode={}, Message={}",
                    ex.statusCode(),
                    ex.getMessage(),
                    ex
            );

            throw new ResponseStatusException(
                    ex.statusCode() >= 400
                            && ex.statusCode() < 500
                            ? org.springframework.http.HttpStatus.BAD_REQUEST
                            : org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "PayPal create order failed"
            );

        } catch (IOException ex) {

            log.error(
                    "PayPal create order IO exception",
                    ex
            );

            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "PayPal create order failed"
            );
        }
    }

    @Override
    public PayPalOrderResponse capturePayPalOrder(
            String payPalOrderId,
            Long appOrderId
    ) {

        try {

            log.info(
                    "Capturing PayPal order. PayPalOrderId={}, AppOrderId={}",
                    payPalOrderId,
                    appOrderId
            );

            OrdersCaptureRequest captureRequest =
                    new OrdersCaptureRequest(payPalOrderId);

            captureRequest.prefer("return=representation");

            captureRequest.requestBody(new OrderRequest());

            log.info("Calling PayPal capture API");

            HttpResponse<Order> response =
                    payPalHttpClient.execute(captureRequest);

            log.info(
                    "PayPal capture success. HTTP Status={}",
                    response.statusCode()
            );

            PayPalOrderResponse out =
                    mapOrderResponse(response.result());

            log.info(
                    "PayPal order captured successfully. OrderId={}, Status={}",
                    out.getOrderId(),
                    out.getStatus()
            );

            persistCaptureTransaction(appOrderId, out);

            log.info(
                    "Payment transaction updated in database. OrderId={}, AppOrderId={}",
                    out.getOrderId(),
                    appOrderId
            );

            return out;

        } catch (HttpException ex) {

            log.error(
                    "PayPal capture failed. StatusCode={}, Message={}",
                    ex.statusCode(),
                    ex.getMessage(),
                    ex
            );

            throw new ResponseStatusException(
                    ex.statusCode() >= 400
                            && ex.statusCode() < 500
                            ? org.springframework.http.HttpStatus.BAD_REQUEST
                            : org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "PayPal capture order failed"
            );

        } catch (IOException ex) {

            log.error(
                    "PayPal capture IO exception",
                    ex
            );

            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "PayPal capture order failed"
            );
        }
    }

    private PayPalOrderResponse mapOrderResponse(Order order) {

        String approveUrl = null;

        List<LinkDescription> links = order.links();

        if (links != null) {

            for (LinkDescription link : links) {

                if (link != null
                        && "approve".equalsIgnoreCase(link.rel())) {

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

    private void persistCreatedTransaction(
            PayPalOrderRequest request,
            PayPalOrderResponse out
    ) {

        if (out == null
                || out.getOrderId() == null
                || out.getOrderId().isBlank()) {

            return;
        }

        if (request == null || request.getAmount() == null) {

            return;
        }

        String providerOrderId = out.getOrderId().trim();

        Optional<PaymentTransaction> existing =
                paymentTransactionRepository
                        .findByProviderOrderId(providerOrderId);

        PaymentTransaction txn =
                existing.orElseGet(PaymentTransaction::new);

        LocalDateTime now = LocalDateTime.now();

        if (txn.getCreatedAt() == null) {

            txn.setCreatedAt(now);
        }

        txn.setProvider(PaymentProvider.PAYPAL);

        txn.setProviderOrderId(providerOrderId);

        txn.setStatus(PaymentStatus.CREATED);

        txn.setAmount(request.getAmount());

        txn.setCurrency(
                request.getCurrency() == null
                        ? properties.getCurrency()
                        : request.getCurrency()
        );

        txn.setUpdatedAt(now);

        paymentTransactionRepository.save(txn);
    }

    private void persistCaptureTransaction(
            Long appOrderId,
            PayPalOrderResponse out
    ) {

        if (out == null
                || out.getOrderId() == null
                || out.getOrderId().isBlank()) {

            return;
        }

        String providerOrderId = out.getOrderId().trim();

        PaymentStatus status =
                mapPayPalStatus(out.getStatus());

        Optional<PaymentTransaction> existing =
                paymentTransactionRepository
                        .findByProviderOrderId(providerOrderId);

        PaymentTransaction txn =
                existing.orElseGet(PaymentTransaction::new);

        LocalDateTime now = LocalDateTime.now();

        if (txn.getCreatedAt() == null) {

            txn.setCreatedAt(now);
        }

        txn.setProvider(PaymentProvider.PAYPAL);

        txn.setProviderOrderId(providerOrderId);

        txn.setStatus(status);

        if (appOrderId != null) {

            txn.setAppOrderId(appOrderId);
        }

        txn.setUpdatedAt(now);

        if (txn.getAmount() == null) {

            txn.setAmount(java.math.BigDecimal.ZERO);
        }

        if (txn.getCurrency() == null) {

            txn.setCurrency(
                    properties.getCurrency() == null
                            ? "USD"
                            : properties.getCurrency()
            );
        }

        paymentTransactionRepository.save(txn);
    }

    private PaymentStatus mapPayPalStatus(String status) {

        if (status == null) {

            return PaymentStatus.FAILED;
        }

        String s = status.trim().toUpperCase();

        if ("COMPLETED".equals(s)) {

            return PaymentStatus.COMPLETED;
        }

        if ("CREATED".equals(s)
                || "APPROVED".equals(s)
                || "SAVED".equals(s)
                || "PAYER_ACTION_REQUIRED".equals(s)) {

            return PaymentStatus.CREATED;
        }

        return PaymentStatus.FAILED;
    }
}