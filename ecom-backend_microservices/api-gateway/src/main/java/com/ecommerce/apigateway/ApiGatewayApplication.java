package com.ecommerce.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ================= AUTH SERVICE =================
                .route("auth_route", r -> r
                        .path("/api/auth/**")
                        .uri("http://localhost:8081"))

                // ================= USER SERVICE =================
                .route("user_route", r -> r
                        .path("/api/users/**")
                        .uri("http://localhost:8082"))

                // ================= PRODUCT SERVICE =================
                .route("product_route", r -> r
                        .path("/api/products/**", "/api/public/products/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("productService")
                                        .setFallbackUri("forward:/fallback/products")))
                        .uri("http://localhost:8083"))

                // ================= CATEGORY SERVICE =================
                .route("category_route", r -> r
                        .path("/api/categories/**")
                        .uri("http://localhost:8083"))

                // ================= CART SERVICE =================
                .route("cart_route", r -> r
                        .path("/api/carts/**")
                        .uri("http://localhost:8085"))

                // ================= ORDER SERVICE =================
                .route("order_route", r -> r
                        .path("/api/orders/**", "/api/admin/orders/**")
                        .uri("http://localhost:8085"))

                // ================= PAYMENT SERVICE =================
                .route("payment_route", r -> r
                        .path("/api/payments/**")
                        .uri("http://localhost:8086"))

                // ================= NOTIFICATION SERVICE =================
                .route("notification_route", r -> r
                        .path("/api/notifications/**")
                        .uri("http://localhost:8087"))

                // ================= STATIC / IMAGES =================
                .route("images_route", r -> r
                        .path("/images/**")
                        .uri("http://localhost:8083"))

                .build();
    }
}