package com.example.noteice.controllers;

import com.example.noteice.dtos.AuthInputDto;
import com.example.noteice.dtos.RefreshJwtRequest;
import com.example.noteice.services.auth.SignInService;
import com.example.noteice.services.auth.SignUpService;
import com.example.noteice.services.auth.TokenRefreshService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/noteice/auth")
public class AuthController {
    private final SignUpService signUpService;
    private final SignInService signInService;
    private final TokenRefreshService tokenRefreshService;
    private final MessageSource messageSource;

    public AuthController(
            SignUpService signUpService,
            SignInService signInService,
            TokenRefreshService tokenRefreshService,
            MessageSource messageSource) {
        this.signUpService = signUpService;
        this.signInService = signInService;
        this.tokenRefreshService = tokenRefreshService;
        this.messageSource = messageSource;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody @Valid AuthInputDto authInputDto,
                                    HttpServletRequest request) {
        signUpService.signUp(authInputDto, request.getLocale());
        return ResponseEntity.ok(null);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody @Valid AuthInputDto authInputDto) {
        val authResponse = signInService.signIn(new AuthInputDto(authInputDto.login(), authInputDto.password()));
        return ResponseEntity.ok(authResponse);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshJwtRequest refreshJwtRequest)  {
        val response = tokenRefreshService.refreshToken(refreshJwtRequest.refreshToken());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam("token") String token,
                                     ServletRequest servletRequest) {
        signUpService.verifyToken(token);
        return ResponseEntity.ok(messageSource.getMessage("emailConfirmation.success", null, servletRequest.getLocale()));
    }
}
