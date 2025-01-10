package com.example.noteice.controllers;

import com.example.noteice.dtos.AuthInputDto;
import com.example.noteice.dtos.RefreshJwtRequest;
import com.example.noteice.services.SignInService;
import com.example.noteice.services.SignUpService;
import com.example.noteice.services.TokenRefreshService;
import com.example.noteice.utils.TokenNotValidException;
import com.example.noteice.utils.UserAlreadyExistsException;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public ResponseEntity<?> signUp(@RequestBody AuthInputDto authInputDto) {
        signUpService.signUp(authInputDto);
        val authResponse = signInService.signIn(new AuthInputDto(authInputDto.login(), authInputDto.password()));
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody AuthInputDto authInputDto) {
        val authResponse = signInService.signIn(new AuthInputDto(authInputDto.login(), authInputDto.password()));
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshJwtRequest refreshJwtRequest) {
        try {
            val response = tokenRefreshService.refreshToken(refreshJwtRequest.refreshToken());
            return ResponseEntity.ok(response);
        } catch (TokenNotValidException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token not valid");
        }
    }
}
