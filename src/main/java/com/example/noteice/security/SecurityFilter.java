package com.example.noteice.security;

import com.example.noteice.repositories.UserRepository;
import com.example.noteice.utils.TokenNotValidException;
import com.example.noteice.utils.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    public SecurityFilter(TokenProvider tokenProvider,
                          UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        val extractedAccessToken = extractToken(request);
        try {
            if (extractedAccessToken != null) {
                val username = tokenProvider.verifyToken(extractedAccessToken);
                val user = userRepository.findByLogin(username);
                if (user == null) {
                    resolver.resolveException(request, response, null, new UnauthorizedException("User does not exist"));
                    return;
                }

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            }
            filterChain.doFilter(request, response);
        } catch (TokenNotValidException e) {
            resolver.resolveException(request, response, null, e);
        }
    }

    private String extractToken(HttpServletRequest request) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        } else return null;
    }
}
