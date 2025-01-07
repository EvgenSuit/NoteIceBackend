package com.example.noteice.controllers;

import com.example.noteice.dtos.JwtResponse;
import com.example.noteice.dtos.RefreshJwtRequest;
import com.example.noteice.dtos.SignInDto;
import com.example.noteice.dtos.SignUpDto;
import com.example.noteice.services.SignInService;
import com.example.noteice.services.SignUpService;
import com.example.noteice.services.TokenRefreshService;
import com.example.noteice.utils.RefreshTokenNotValidException;
import com.example.noteice.utils.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/noteice/auth")
public class AuthController {
    private final SignUpService signUpService;
    private final SignInService signInService;
    private final TokenRefreshService tokenRefreshService;

    public AuthController(
            SignUpService signUpService,
            SignInService signInService,
            TokenRefreshService tokenRefreshService) {
        this.signUpService = signUpService;
        this.signInService = signInService;
        this.tokenRefreshService = tokenRefreshService;
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtResponse> signUp(@RequestBody SignUpDto signUpDto) {
        try {
            signUpService.signUp(signUpDto);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        val authResponse = signInService.signIn(new SignInDto(signUpDto.login(), signUpDto.password()));
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> singIn(@RequestBody SignInDto signInDto) {
        val authResponse = signInService.signIn(new SignInDto(signInDto.login(), signInDto.password()));
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshJwtRequest refreshJwtRequest) {
        try {
            val response = tokenRefreshService.refreshToken(refreshJwtRequest);
            return ResponseEntity.ok(response);
        } catch (RefreshTokenNotValidException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
