package com.ecommerce.payment.config;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PayPalProperties.class)
public class PayPalConfig {

    @Bean
    public PayPalHttpClient payPalHttpClient(PayPalProperties properties) {
        PayPalEnvironment environment = properties.isSandbox()
                ? new PayPalEnvironment.Sandbox(properties.getClientId(), properties.getClientSecret())
                : new PayPalEnvironment.Live(properties.getClientId(), properties.getClientSecret());

        return new PayPalHttpClient(environment);
    }
}
