package com.ecommerce.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret:${app.jwt.secret:}}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:${app.jwt.expiration-ms:86400000}}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration-ms:${app.jwt.refresh-expiration-ms:604800000}}")
    private long refreshTokenExpirationMs;

    // 🔐 Generate JWT token with claims
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userPrincipal.getId());
        claims.put("email", userPrincipal.getEmail());
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return createToken(claims, userPrincipal.getUsername());
    }

    // 🔐 Generate token using username only
    public String generateTokenFromUsername(String username) {
        return createToken(new HashMap<>(), username);
    }

    // 🔐 Core token creation method
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    // 🔐 Generate refresh token
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(key())
                .compact();
    }

    // 🔍 Extract username
    public String getUserNameFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    // 🔍 Extract user ID
    public Long getUserIdFromJwtToken(String token) {
        return parseClaims(token).get("id", Long.class);
    }

    // 🔍 Extract all claims (central method)
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 🔍 Extract token from HTTP request
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // ✅ Validate token
    public boolean validateJwtToken(String authToken) {
        try {
            parseClaims(authToken);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // 🔐 Secret key
    private SecretKey key() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        } catch (IllegalArgumentException ex) {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }

    // 🔍 Get all claims
    public Claims getAllClaimsFromToken(String token) {
        return parseClaims(token);
    }

    // 🔍 Check expiration
    public Boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    // 🔍 Get expiration date
    public Date getExpirationDateFromToken(String token) {
        return parseClaims(token).getExpiration();
    }
}
