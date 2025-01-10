package com.example.noteice.controllers;

import com.example.noteice.utils.TokenNotValidException;
import com.example.noteice.utils.UnauthorizedException;
import com.example.noteice.utils.UserAlreadyExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TokenNotValidException.class)
    public ResponseEntity<?> handleTokenNotValidException(TokenNotValidException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Access token not valid: " + e.getMessage());
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }
    @ExceptionHandler({ AuthenticationException.class, UnauthorizedException.class })
    public ResponseEntity<?> handleAuthenticationException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }
}
