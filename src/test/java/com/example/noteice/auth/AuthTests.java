package com.example.noteice.auth;

import com.example.noteice.dtos.AuthInputDto;
import com.example.noteice.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class AuthTests {

    @Autowired
    UserRepository userRepository;
    @Autowired
    MessageSource messageSource;

    @Autowired
    MockMvc mockMvc;
    private final String rootUrl = "/noteice/auth/";
    private final AuthInputDto defaultAuthInputDto = new AuthInputDto("user@gmail.com", "Password123$");

    @Test
    void signUpEmailBlankPLLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(" ", defaultAuthInputDto.password());
        String expectedErrorMessage = messageSource.getMessage("login.notBlank", null, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedErrorMessage, "pl");
    }
    @Test
    void signUpInvalidEmailPLLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto("user", defaultAuthInputDto.password());
        String expectedErrorMessage = messageSource.getMessage("login.email", null, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedErrorMessage, "pl");
    }

    @Test
    void signUpInvalidPasswordPatternPLLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "password123");
        String expectedPasswordError = messageSource.getMessage("password.pattern", null, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "pl");
    }

    @Test
    void signUpPasswordBlankPLLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "  ");
        String expectedPasswordError = messageSource.getMessage("password.notBlank", null, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "pl");
    }
    @Test
    void signUpPasswordTooShortPLLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "Pas3$");
        String expectedPasswordError = messageSource.getMessage("password.size", null, new Locale("pl"))
                .replace("{min}", "8").replace("{max}", "64");
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "pl");
    }
    @Test
    void signUpPasswordTooLongPLLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), defaultAuthInputDto.password().repeat(64));
        String expectedPasswordError = messageSource.getMessage("password.size", null, new Locale("pl"))
                .replace("{min}", "8").replace("{max}", "64");
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "pl");
    }

    @Test
    void signUpEmailBlankENLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(" ", defaultAuthInputDto.password());
        String expectedErrorMessage = messageSource.getMessage("login.notBlank", null, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedErrorMessage, "en");
    }
    @Test
    void signUpInvalidEmailENLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto("user", defaultAuthInputDto.password());
        String expectedErrorMessage = messageSource.getMessage("login.email", null, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedErrorMessage, "en");
    }

    @Test
    void signUpInvalidPasswordPatternENLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "password123");
        String expectedPasswordError = messageSource.getMessage("password.pattern", null, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "en");
    }

    @Test
    void signUpPasswordBlankENLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "  ");
        String expectedPasswordError = messageSource.getMessage("password.notBlank", null, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "en");
    }
    @Test
    void signUpPasswordTooShortENLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "Pas3$");
        String expectedPasswordError = messageSource.getMessage("password.size", null, new Locale("en"))
                .replace("{min}", "8").replace("{max}", "64");
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "en");
    }
    @Test
    void signUpPasswordTooLongENLocale() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), defaultAuthInputDto.password().repeat(64));
        String expectedPasswordError = messageSource.getMessage("password.size", null, new Locale("en"))
                .replace("{min}", "8").replace("{max}", "64");
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "en");
    }

    private void performSignUpWithInvalidInput(
            AuthInputDto authInputDto,
            String expectedError,
            String locale
    ) throws Exception {
        mockMvc.perform(post(rootUrl + "signup")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                        .content(new ObjectMapper().writeValueAsBytes(authInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedError)))
                .andReturn();
    }

    @Test
    void signUpUserAlreadyExists() throws Exception {
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        UserDetails user = userRepository.findByLogin(defaultAuthInputDto.login());
        assertEquals(defaultAuthInputDto.login(), user.getUsername());

        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isConflict());
        UserDetails user1 = userRepository.findByLogin(defaultAuthInputDto.login());
        assertEquals(defaultAuthInputDto.login(), user1.getUsername());
    }
    @Test
    void signInUserDoesNotExist() throws Exception {
        mockMvc.perform(post(rootUrl + "signin")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        assertNull(userRepository.findByLogin(defaultAuthInputDto.login()));
    }
    @Test
    void signInUserExists() throws Exception {
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        UserDetails user = userRepository.findByLogin(defaultAuthInputDto.login());
        assertEquals(defaultAuthInputDto.login(), user.getUsername());

        mockMvc.perform(post(rootUrl + "signin")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(defaultAuthInputDto.login(), userRepository.findByLogin(defaultAuthInputDto.login()).getUsername());
    }

}
