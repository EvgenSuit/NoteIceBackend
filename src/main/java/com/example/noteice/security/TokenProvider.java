package com.example.noteice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class TokenProvider {

    @Value("${jwt-secret}")
    private String secretKey;
    @Value("${access-token-expiration-hours}")
    private Integer accessTokenExpirationHours;
    @Value("${refresh-token-expiration-days}")
    private Integer refreshTokenExpirationDays;
    @Value("${verification-token-expiration-minutes}")
    private Integer verificationTokenExpirationMinutes;
    private final Clock clock;

    public TokenProvider(Clock clock) {
        this.clock = clock;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateAccessToken(String login) {
        Instant issuedAt = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = issuedAt.plus(accessTokenExpirationHours, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(login)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(getKey())
                .compact();
    }
    public String generateRefreshToken(String login) {
        Instant issuedAt = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = issuedAt.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);
        return Jwts.builder()
                .subject(login)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(getKey())
                .compact();
    }
    public String generateVerificationToken(String login) {
        Instant issuedAt = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = issuedAt.plus(verificationTokenExpirationMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(login)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(getKey())
                .compact();
    }

    public String verifyToken(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .clock(() -> Date.from(Instant.now(clock)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}
