package com.example.noteice.auth;

import com.example.noteice.security.TokenProvider;
import com.example.noteice.utils.TokenNotValidException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TokenProviderTests {
    private TokenProvider tokenProvider;
    private final String TEST_USERNAME = "username";
    private final String SECRET = "1Kj5vsHSjFd0ASJQfxWpthyJ/ATImtem9cIlvdqMEgUlB1n2DANMVxfAow8idHxNUaD0UopPRKNtP5lcv0x8NA==";
    private final Integer ACCESS_TOKEN_EXPIRATION_HOURS = 1;
    private final Integer REFRESH_TOKEN_EXPIRATION_HOURS = 2;

    @BeforeEach
    void setup() {
        tokenProvider = new TokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "secretKey", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "accessTokenExpirationHours", ACCESS_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpirationHours", REFRESH_TOKEN_EXPIRATION_HOURS);
    }

    @Test
    void generateAccessToken_ShouldCreateValidToken() throws TokenNotValidException {
        String token = tokenProvider.generateAccessToken(TEST_USERNAME);

        assertNotNull(token);
        String username = tokenProvider.verifyToken(token);
        assertEquals(TEST_USERNAME, username);
    }
    @Test
    void generateRefreshToken_ShouldCreateValidToken() throws TokenNotValidException {
        String token = tokenProvider.generateRefreshToken(TEST_USERNAME);

        assertNotNull(token);
        String username = tokenProvider.verifyToken(token);
        assertEquals(TEST_USERNAME, username);
    }
    @Test
    void generateAccessToken_WithInvalidSignature_ShouldThrowException() {
        TokenProvider tokenProviderWithInvalidSignature = new TokenProvider();
        ReflectionTestUtils.setField(tokenProviderWithInvalidSignature, "accessTokenExpirationHours", ACCESS_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(tokenProviderWithInvalidSignature, "refreshTokenExpirationHours", REFRESH_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(tokenProviderWithInvalidSignature, "secretKey", "");

        assertThrows(JwtException.class, () -> tokenProviderWithInvalidSignature.generateRefreshToken(TEST_USERNAME));
    }
    @Test
    void generateRefreshToken_WithInvalidSignature_ShouldThrowException() {
        TokenProvider tokenProviderWithInvalidSignature = new TokenProvider();
        ReflectionTestUtils.setField(tokenProviderWithInvalidSignature, "accessTokenExpirationHours", ACCESS_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(tokenProviderWithInvalidSignature, "refreshTokenExpirationHours", ACCESS_TOKEN_EXPIRATION_HOURS);
        ReflectionTestUtils.setField(tokenProviderWithInvalidSignature, "secretKey", "");

        assertThrows(JwtException.class, () -> tokenProviderWithInvalidSignature.generateRefreshToken(TEST_USERNAME));
    }
    @Test
    void verifyToken_WithExpiredAccessToken_ShouldThrowException() {
        TokenProvider tokenProviderWithExpiredToken = new TokenProvider();
        ReflectionTestUtils.setField(tokenProviderWithExpiredToken, "secretKey", SECRET);
        ReflectionTestUtils.setField(tokenProviderWithExpiredToken, "accessTokenExpirationHours", -1);

        String token = tokenProviderWithExpiredToken.generateAccessToken(TEST_USERNAME);
        assertThrows(TokenNotValidException.class, () -> tokenProvider.verifyToken(token));
    }
    @Test
    void verifyToken_WithExpiredRefreshToken_ShouldThrowException() {
        TokenProvider tokenProviderWithExpiredToken = new TokenProvider();
        ReflectionTestUtils.setField(tokenProviderWithExpiredToken, "secretKey", SECRET);
        ReflectionTestUtils.setField(tokenProviderWithExpiredToken, "refreshTokenExpirationHours", -1);

        String token = tokenProviderWithExpiredToken.generateRefreshToken(TEST_USERNAME);
        assertThrows(TokenNotValidException.class, () -> tokenProvider.verifyToken(token));
    }
}
