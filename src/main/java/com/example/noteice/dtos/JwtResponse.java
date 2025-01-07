package com.example.noteice.dtos;

public record JwtResponse(
        String accessToken,
        String refreshToken
) {
}
