package com.example.noteice.services;

import com.example.noteice.dtos.JwtResponse;
import com.example.noteice.dtos.RefreshJwtRequest;
import com.example.noteice.security.TokenProvider;
import com.example.noteice.utils.TokenNotValidException;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class TokenRefreshService {
    private final TokenProvider tokenProvider;

    public TokenRefreshService(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public JwtResponse refreshToken(String refreshToken) throws TokenNotValidException {
        val login = tokenProvider.verifyToken(refreshToken);
        val newAccessToken = tokenProvider.generateAccessToken(login);
        val newRefreshToken = tokenProvider.generateRefreshToken(login);
        return new JwtResponse(newAccessToken, newRefreshToken);
    }
}
