package com.example.noteice.controllers;

import com.example.noteice.utils.UnauthorizedException;
import com.example.noteice.utils.auth.UserAlreadyExistsException;
import com.example.noteice.utils.notes.NoteNotFoundException;
import io.jsonwebtoken.JwtException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    MessageSource messageSource;
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleTokenNotValidException(JwtException e,
                                                          Locale locale) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(messageSource.getMessage("tokenCreationException", null, locale) + e.getMessage());
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
    @ExceptionHandler({ AuthenticationException.class, UnauthorizedException.class })
    public ResponseEntity<?> handleAuthenticationException(Exception e) {
        System.out.println(e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e,
                                                       Locale locale) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            errors.put(((FieldError)error).getField(), messageSource.getMessage(error, locale));
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<?> handleNoteNotFoundException() {
        return ResponseEntity.notFound().build();
    }
}
