package com.example.noteice.auth;

import com.example.noteice.dtos.AuthInputDto;
import com.example.noteice.dtos.User;
import com.example.noteice.repositories.UserRepository;
import com.example.noteice.security.TokenProvider;
import com.example.noteice.services.auth.UserVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTests {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    TokenProvider tokenProvider;

    // Security filter uses userRepository to extract user authorities by login
    @Autowired
    UserRepository userRepository;

    // use fake UserVerificationService for sign up testing
    @MockitoBean
    UserVerificationService userVerificationService;

    private final String rootNotesUrl = "/noteice/notes/";
    private final String rootAuthUrl = "/noteice/auth/";

    @Test
    void validateToken_TokenInvalid_ShouldReturn401Error() throws Exception {
        String invalidToken = "invalid-token";
        when(tokenProvider.verifyToken(invalidToken)).thenThrow(new JwtException(""));

        mockMvc.perform(get(rootNotesUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void validateToken_TokenValid_ShouldNotReturn401Error() throws Exception {
        String validToken = "some-token";
        String username = "username";
        when(tokenProvider.verifyToken(validToken)).thenReturn(username);
        userRepository.save(new User(null, username, "password"));

        mockMvc.perform(get(rootNotesUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk());
    }
    @Test
    void makeRequestToNotes_notAuthenticated_noTokenSent_ShouldReturn403() throws Exception {
        mockMvc.perform(get(rootNotesUrl))
                .andExpect(status().isForbidden());
    }
    @Test
    void makeRequestToNotes_notAuthenticated_validAccessTokenSent_ShouldReturn401() throws Exception {
        String validToken = "valid-token";
        when(tokenProvider.verifyToken(validToken)).thenReturn(validToken);
        mockMvc.perform(get(rootNotesUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext
    void makeRequestToAuth_notAuthenticated_ShouldNotReturn403() throws Exception {
        User user = new User(null, "user@gmail.com", "Password123$");
        mockMvc.perform(post(rootAuthUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(new AuthInputDto(user.getLogin(), user.getPassword())))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());
    }
}
