package com.ecommerce.product.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final HeaderAuthenticationFilter headerAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ❌ Disable CORS here (Gateway handles it)
                .cors(cors -> cors.disable())

                .csrf(csrf -> csrf.disable())

                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth ->
                        auth
                                // ✅ PUBLIC (IMPORTANT FIX)
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/products/").permitAll()
                                // Admin only
                                .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("ADMIN", "SELLER")
                                .requestMatchers(HttpMethod.PUT, "/api/products/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/products/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                                // Docs
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                // Admin only endpoints
                                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/products/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/products/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/products/{id}/image").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/products/{id}/activate").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/products/{id}/deactivate").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/categories/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/categories/{id}").hasRole("ADMIN")

                                // Seller endpoints
                                .requestMatchers(HttpMethod.GET, "/api/products/seller/{sellerId}").hasAnyRole("ADMIN", "SELLER")
                                .requestMatchers(HttpMethod.GET, "/api/products/low-stock").hasAnyRole("ADMIN", "SELLER")

                                // Documentation
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()


                                .anyRequest().authenticated()
                );

        http.addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 