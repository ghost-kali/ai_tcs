package com.ecommerce.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(PayPalProperties.class)
public class PayPalConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

