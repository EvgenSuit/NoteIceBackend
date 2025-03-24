package com.example.noteice.auth;

import com.example.noteice.security.TokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TokenProviderTests {
    private TokenProvider tokenProvider;
    private final String TEST_USERNAME = "username";
    private final String SECRET = "1Kj5vsHSjFd0ASJQfxWpthyJ/ATImtem9cIlvdqMEgUlB1n2DANMVxfAow8idHxNUaD0UopPRKNtP5lcv0x8NA==";
    private final Integer ACCESS_TOKEN_EXPIRATION_HOURS = 1;
    private final Integer REFRESH_TOKEN_EXPIRATION_DAYS = 2;
    private final Integer VERIFICATION_TOKEN_EXPIRATION_MINUTES = 1;

    @Autowired
    private Clock clock;

    @BeforeEach
    void setup() {
        tokenProvider = new TokenProvider(clock);
        ReflectionTestUtils.setField(tokenProvider, "secretKey", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "accessTokenExpirationHours", ACCESS_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpirationDays", REFRESH_TOKEN_EXPIRATION_DAYS);
        ReflectionTestUtils.setField(tokenProvider, "verificationTokenExpirationMinutes", VERIFICATION_TOKEN_EXPIRATION_MINUTES);
    }

    @Test
    void generateAccessToken_validTokenGenerated() {
        String token = tokenProvider.generateAccessToken(TEST_USERNAME);

        assertNotNull(token);
        String username = tokenProvider.verifyToken(token);
        assertEquals(TEST_USERNAME, username);
    }
    @Test
    void generateRefreshToken_validTokenGenerated() {
        String token = tokenProvider.generateRefreshToken(TEST_USERNAME);

        assertNotNull(token);
        String username = tokenProvider.verifyToken(token);
        assertEquals(TEST_USERNAME, username);
    }
    @Test
    void generateVerificationToken_validTokenGenerated() {
        String token = tokenProvider.generateVerificationToken(TEST_USERNAME);

        assertNotNull(token);
        String username = tokenProvider.verifyToken(token);
        assertEquals(TEST_USERNAME, username);
    }
    @Test
    void generateAccessToken_invalidSignature_exceptionThrown() {
        ReflectionTestUtils.setField(tokenProvider, "accessTokenExpirationHours", ACCESS_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(tokenProvider, "secretKey", "");

        assertThrows(WeakKeyException.class, () -> tokenProvider.generateAccessToken(TEST_USERNAME));
    }
    @Test
    void generateRefreshToken_invalidSignature_exceptionThrown() {
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpirationDays", ACCESS_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(tokenProvider, "secretKey", "");

        assertThrows(WeakKeyException.class, () -> tokenProvider.generateRefreshToken(TEST_USERNAME));
    }
    @Test
    void generateVerificationToken_invalidSignature_exceptionThrown() {
        ReflectionTestUtils.setField(tokenProvider, "verificationTokenExpirationMinutes", VERIFICATION_TOKEN_EXPIRATION_MINUTES);
        ReflectionTestUtils.setField(tokenProvider, "secretKey", "");

        assertThrows(WeakKeyException.class, () -> tokenProvider.generateVerificationToken(TEST_USERNAME));
    }

    @Test
    void verifyToken_expiredAccessToken_exceptionThrown() {
        String token = tokenProvider.generateAccessToken(TEST_USERNAME);

        Clock newClock = Clock.offset(clock, Duration.ofHours(ACCESS_TOKEN_EXPIRATION_HOURS));
        TokenProvider expiredTokenProvider = new TokenProvider(newClock);
        ReflectionTestUtils.setField(expiredTokenProvider, "accessTokenExpirationHours", ACCESS_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(expiredTokenProvider, "secretKey", SECRET);

        assertThrows(ExpiredJwtException.class, () -> expiredTokenProvider.verifyToken(token));
    }
    @Test
    void verifyToken_expiredRefreshToken_exceptionThrown() {
        String token = tokenProvider.generateRefreshToken(TEST_USERNAME);

        Clock expiredClock = Clock.offset(clock, Duration.ofDays(REFRESH_TOKEN_EXPIRATION_DAYS));
        TokenProvider expiredTokenProvider = new TokenProvider(expiredClock);
        ReflectionTestUtils.setField(expiredTokenProvider, "secretKey", SECRET);
        ReflectionTestUtils.setField(expiredTokenProvider, "refreshTokenExpirationDays", REFRESH_TOKEN_EXPIRATION_DAYS);

        assertThrows(ExpiredJwtException.class, () -> expiredTokenProvider.verifyToken(token));
    }

    @Test
    void verifyToken_expiredVerificationToken_exceptionThrown() {
        String verificationToken = tokenProvider.generateVerificationToken(TEST_USERNAME);

        Clock expiredClock = Clock.offset(clock, Duration.ofMinutes(VERIFICATION_TOKEN_EXPIRATION_MINUTES));
        TokenProvider expiredTokenProvider = new TokenProvider(expiredClock);
        ReflectionTestUtils.setField(expiredTokenProvider, "secretKey", SECRET);
        ReflectionTestUtils.setField(expiredTokenProvider,"verificationTokenExpirationMinutes", VERIFICATION_TOKEN_EXPIRATION_MINUTES);

        assertThrows(ExpiredJwtException.class, () -> expiredTokenProvider.verifyToken(verificationToken));
    }

}
