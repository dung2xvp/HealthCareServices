package org.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtTokenProvider - Tạo và validate JWT tokens
 * Chức năng:
 * 1. Generate JWT token từ user authentication
 * 2. Validate JWT token
 * 3. Extract thông tin từ token (email, expiration...)
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Tạo JWT token từ Authentication object
     * Token chứa: email user, issue date, expiration date
     */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // Email của user
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Lấy email từ JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }

    /**
     * Validate JWT token
     * Kiểm tra: signature hợp lệ, chưa expired, format đúng
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            // Token không đúng format
            System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            // Token đã hết hạn
            System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            // Token không được hỗ trợ
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            // Token claims empty
            System.out.println("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Lấy signing key từ secret
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

