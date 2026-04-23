package com.ecommerce.payment.service;

import com.ecommerce.payment.config.PayPalProperties;
import com.ecommerce.payment.dto.PayPalOrderRequest;
import com.ecommerce.payment.dto.PayPalOrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RestTemplate restTemplate;
    private final PayPalProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PayPalOrderResponse createPayPalOrder(PayPalOrderRequest request) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{
                        Map.of(
                                "description", request.getDescription() == null ? "Order payment" : request.getDescription(),
                                "amount", Map.of(
                                        "currency_code", request.getCurrency() == null ? properties.getCurrency() : request.getCurrency(),
                                        "value", request.getAmount().toPlainString()
                                )
                        )
                },
                "application_context", Map.of(
                        "brand_name", properties.getBrandName(),
                        "return_url", properties.getReturnUrl(),
                        "cancel_url", properties.getCancelUrl(),
                        "user_action", "PAY_NOW"
                )
        );

        ResponseEntity<String> response = restTemplate.exchange(
                properties.getBaseUrl() + "/v2/checkout/orders",
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                String.class
        );

        return mapOrderResponse(response.getBody());
    }

    @Override
    public PayPalOrderResponse capturePayPalOrder(String payPalOrderId) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                properties.getBaseUrl() + "/v2/checkout/orders/" + payPalOrderId + "/capture",
                HttpMethod.POST,
                new HttpEntity<>("{}", headers),
                String.class
        );

        return mapOrderResponse(response.getBody());
    }

    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        ResponseEntity<String> response = restTemplate.exchange(
                properties.getBaseUrl() + "/v1/oauth2/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to read PayPal access token");
        }
    }

    private String basicAuth() {
        String raw = properties.getClientId() + ":" + properties.getClientSecret();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private PayPalOrderResponse mapOrderResponse(String body) {
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            String approveUrl = null;

            JsonNode links = jsonNode.get("links");
            if (links != null && links.isArray()) {
                for (JsonNode link : links) {
                    if ("approve".equalsIgnoreCase(link.get("rel").asText())) {
                        approveUrl = link.get("href").asText();
                        break;
                    }
                }
            }

            return PayPalOrderResponse.builder()
                    .orderId(jsonNode.path("id").asText())
                    .status(jsonNode.path("status").asText())
                    .approveUrl(approveUrl)
                    .build();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to parse PayPal response");
        }
    }
}

