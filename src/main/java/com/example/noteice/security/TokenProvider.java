package com.example.noteice.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.Date;

@Component
public class TokenProvider {

    @Value("${jwt-secret}")
    private String secretKey;
    @Value("${access-token-expiration-hours}")
    private Integer accessTokenExpirationHours;
    @Value("${refresh-token-expiration-hours}")
    private Integer refreshTokenExpirationHours;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .claim("username", username)
                .expiration(Date.from(LocalDateTime.now().plusHours(accessTokenExpirationHours).toInstant(ZoneOffset.UTC)))
                .signWith(getKey())
                .compact();
    }
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .claim("username", username)
                .expiration(Date.from(LocalDateTime.now().plusDays(refreshTokenExpirationHours).toInstant(ZoneOffset.UTC)))
                .signWith(getKey())
                .compact();
    }

    public String verifyToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
    }
}
