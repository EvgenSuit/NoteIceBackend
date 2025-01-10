package com.example.noteice.auth;

import com.example.noteice.dtos.AuthInputDto;
import com.example.noteice.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthTests {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;
    private final String rootUrl = "/noteice/auth/";

    @Test
    @DirtiesContext
    void signUpUserAlreadyExists() throws Exception {
        AuthInputDto authInputDto = new AuthInputDto("user", "password123");
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(authInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        UserDetails user = userRepository.findByLogin(authInputDto.login());
        assertEquals(authInputDto.login(), user.getUsername());

        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(authInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isConflict());
        UserDetails user1 = userRepository.findByLogin(authInputDto.login());
        assertEquals(authInputDto.login(), user1.getUsername());
    }
    @Test
    void signInUserDoesNotExist() throws Exception {
        AuthInputDto authInputDto = new AuthInputDto("user", "password123");
        mockMvc.perform(post(rootUrl + "signin")
                        .content(new ObjectMapper().writeValueAsBytes(authInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        assertNull(userRepository.findByLogin(authInputDto.login()));
    }
    @Test
    void signInUserExists() throws Exception {
        AuthInputDto authInputDto = new AuthInputDto("user", "password123");
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(authInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        UserDetails user = userRepository.findByLogin(authInputDto.login());
        assertEquals(authInputDto.login(), user.getUsername());

        mockMvc.perform(post(rootUrl + "signin")
                        .content(new ObjectMapper().writeValueAsBytes(authInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(authInputDto.login(), userRepository.findByLogin(authInputDto.login()).getUsername());
    }

}
