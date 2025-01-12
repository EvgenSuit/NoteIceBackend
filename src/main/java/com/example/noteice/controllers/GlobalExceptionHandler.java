package com.example.noteice.controllers;

import com.example.noteice.utils.InputFieldError;
import com.example.noteice.utils.TokenNotValidException;
import com.example.noteice.utils.UnauthorizedException;
import com.example.noteice.utils.UserAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    MessageSource messageSource;
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

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
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e,
                                                       Locale locale) {
        List<InputFieldError> fieldErrors = e.getBindingResult().getAllErrors().stream()
                .map(error -> new InputFieldError(messageSource.getMessage(error, locale)))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fieldErrors);
    }
}
