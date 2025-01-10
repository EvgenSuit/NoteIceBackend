package com.example.noteice.services;

import com.example.noteice.dtos.AuthInputDto;
import com.example.noteice.dtos.JwtResponse;
import com.example.noteice.repositories.UserRepository;
import com.example.noteice.security.TokenProvider;
import lombok.val;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SignInService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public SignInService(UserRepository userRepository,
                         TokenProvider tokenProvider,
                         // AuthenticationManager bean provided in SecurityConfig needs SignInRepository (a real one) since it implements UserDetailsService (
                         // AuthenticationManager calls userDetailsService.loadUserByUsername(username) when using authenticationManager.authenticate),
                         // which in turn uses AuthenticationManager below. So we first need to create AuthenticationManager,
                         // and insert it below only when it's used (when signIn is called)
                         @Lazy AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username);
    }

    public JwtResponse signIn(AuthInputDto authInputDto) {
        val auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authInputDto.login(), authInputDto.password()));
        val accessToken = tokenProvider.generateAccessToken(auth.getName());
        val refreshToken = tokenProvider.generateRefreshToken(auth.getName());
        return new JwtResponse(accessToken, refreshToken);
    }
}
