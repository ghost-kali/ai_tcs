package com.ecommerce.apigateway.filter;

import com.ecommerce.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Value("${jwt.cookieName:${app.jwt.cookieName:springBootEcom}}")
    private String jwtCookieName;

    @Value("${jwt.cache.ttl:${app.jwt.cache.ttl:300}}")
    private long cacheJwtTtl;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        // ✅ Skip public endpoints
        if (isPublicEndpoint(request)) {
            return chain.filter(exchange);
        }

        // ✅ Extract JWT
        String jwt = extractJwtFromCookie(request);
        if (jwt == null) {
            jwt = extractJwtFromHeader(request);
        }

        if (jwt == null) {
            return onError(exchange, "JWT token is missing", HttpStatus.UNAUTHORIZED);
        }

        String cacheKey = "jwt:validation:" + jwt.hashCode();

        return redisTemplate.opsForValue().get(cacheKey)
                .switchIfEmpty(validateAndCacheToken(jwt, cacheKey))
                .flatMap(cachedResult -> {

                    if ("INVALID".equals(cachedResult)) {
                        return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                    }

                    return processValidToken(exchange, chain, cachedResult);
                })
                .onErrorResume(e -> {
                    log.error("JWT processing error: {}", e.getMessage());
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                });
    }

    @Override
    public int getOrder() {
        return 1;
    }

    // ===================== PUBLIC PATH =====================

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getPath().toString();
        String method = request.getMethod().name();

        // Public GET categories
        if (path.startsWith("/api/categories") && method.equals("GET")) {
            return true;
        }

        // Public GET products
        if (path.startsWith("/api/products") && method.equals("GET")) {
            return true;
        }

        return List.of(
                "/api/auth/signin",
                "/api/auth/signup",
                "/api/auth/refresh",
                "/api/public/products",
                "/api/public/categories",
                "/images",
                "/actuator/health"
        ).stream().anyMatch(path::startsWith);
    }

    // ===================== JWT EXTRACTION =====================

    private String extractJwtFromCookie(ServerHttpRequest request) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies.containsKey(jwtCookieName)) {
            HttpCookie cookie = cookies.getFirst(jwtCookieName);
            return cookie != null ? cookie.getValue() : null;
        }
        return null;
    }

    private String extractJwtFromHeader(ServerHttpRequest request) {
        String bearer = request.getHeaders().getFirst("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    // ===================== VALIDATION =====================

    private Mono<String> validateAndCacheToken(String jwt, String cacheKey) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = jwtUtil.validateToken(jwt);

                Long userId = claims.get("id", Long.class);
                String email = claims.getSubject();
                List<String> roles = claims.get("roles", List.class);

                String cacheValue =
                        (userId != null ? userId : "") + "|" +
                                (email != null ? email : "") + "|" +
                                (roles != null ? String.join(",", roles) : "");

                redisTemplate.opsForValue()
                        .set(cacheKey, cacheValue, Duration.ofSeconds(cacheJwtTtl))
                        .subscribe();

                return cacheValue;

            } catch (JwtException e) {

                redisTemplate.opsForValue()
                        .set(cacheKey, "INVALID", Duration.ofSeconds(60))
                        .subscribe();

                throw new RuntimeException("Invalid JWT token");
            }
        });
    }

    // ===================== MAIN FIX HERE =====================

    private Mono<Void> processValidToken(ServerWebExchange exchange,
                                         GatewayFilterChain chain,
                                         String cachedResult) {

        String[] parts = cachedResult.split("\\|", -1);

        String userId = parts[0];
        String email = parts[1];
        String roles = parts[2];

        // ✅ MUTATE SAFELY (NO DUPLICATES)
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();

        builder.headers(headers -> {
            // 🔥 REMOVE OLD VALUES FIRST
            headers.remove("X-User-Id");
            headers.remove("X-User-Email");
            headers.remove("X-User-Roles");

            // ✅ SET (NOT ADD)
            headers.set("X-User-Id", userId);
            headers.set("X-User-Email", email);
            headers.set("X-User-Roles", roles);
        });

        ServerHttpRequest modifiedRequest = builder.build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    // ===================== ERROR =====================

    private Mono<Void> onError(ServerWebExchange exchange,
                               String error,
                               HttpStatus status) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"error\":\"%s\"}", error);

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}
