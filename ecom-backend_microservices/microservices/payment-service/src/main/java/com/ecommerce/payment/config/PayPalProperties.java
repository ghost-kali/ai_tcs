package com.ecommerce.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "paypal")
public class PayPalProperties {
    private String clientId;
    private String clientSecret;
    private String baseUrl = "https://api-m.sandbox.paypal.com";
    private String returnUrl;
    private String cancelUrl;
    private String brandName = "E-Commerce Store";
    private String currency = "USD";

    /**
     * When true, uses PayPal sandbox environment; when false, uses live.
     * If unset, defaults to true.
     */
    private boolean sandbox = true;
}
