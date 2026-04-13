package com.example.ai_english.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j(topic = "JwtProvider")
public class JwtProvider {
    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_TIME;
    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_TIME;

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken (Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_TIME))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken (Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_TIME))
                .signWith(key)
                .compact();
    }

    public boolean validateToken (String token) {
        try {
            extractClaim(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.debug("Invalid JWT signature/token", e);
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            log.debug("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty", e);
        }
        return false;
    }

    public Claims extractClaim (String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return extractClaim(token).get("userId", Long.class);
    }
}
