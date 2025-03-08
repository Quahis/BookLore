package com.adityachandel.booklore.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import com.adityachandel.booklore.model.entity.BookLoreUserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private long accessTokenExpirationMs;

    @Value("${security.jwt.refreshExpiration}")
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(BookLoreUserEntity user, boolean isRefreshToken) {
        long expirationTime = isRefreshToken ? refreshTokenExpirationMs : accessTokenExpirationMs;
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateAccessToken(BookLoreUserEntity user) {
        return generateToken(user, false);
    }

    public String generateRefreshToken(BookLoreUserEntity user) {
        return generateToken(user, true);
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("Invalid token: " + e.getMessage());
        }
        return false;
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userIdClaim = extractClaims(token).get("userId");
        if (userIdClaim instanceof Number) {
            return ((Number) userIdClaim).longValue();
        }
        throw new IllegalArgumentException("Invalid userId claim type");
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}