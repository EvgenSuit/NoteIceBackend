package com.example.noteice.auth;

import com.example.noteice.dtos.AuthInputDto;
import com.example.noteice.dtos.JwtResponse;
import com.example.noteice.dtos.RefreshJwtRequest;
import com.example.noteice.dtos.User;
import com.example.noteice.repositories.UserRepository;
import com.example.noteice.services.auth.TokenRefreshService;
import com.example.noteice.services.auth.UserVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class AuthTests {

    @Autowired
    UserRepository userRepository;
    @Autowired
    MessageSource messageSource;
    @MockitoBean
    TokenRefreshService tokenRefreshService;
    @MockitoBean
    UserVerificationService userVerificationService;
    private final Object[] passwordLength = new Object[]{8, 64};

    @Autowired
    MockMvc mockMvc;
    private final String rootUrl = "/noteice/auth/";
    private final AuthInputDto defaultAuthInputDto = new AuthInputDto("user@gmail.com", "Password123$");

    @Test
    void signUp_emailBlankPLLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto("", defaultAuthInputDto.password());
        String expectedErrorMessage = messageSource.getMessage("login.notBlank", null, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedErrorMessage, "pl");
    }
    @Test
    void signUp_invalidEmailPLLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto("user", defaultAuthInputDto.password());
        String expectedErrorMessage = messageSource.getMessage("login.email", null, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedErrorMessage, "pl");
    }

    @Test
    void signUp_invalidPasswordPatternPLLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "password123");
        String expectedPasswordError = messageSource.getMessage("password.pattern", null, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "pl");
    }

    @Test
    void signUp_passwordEmptyPLLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), " ");
        String expectedPasswordError = messageSource.getMessage("password.notBlank", null, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "pl");
    }
    @Test
    void signUp_passwordTooShortPLLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "Pas3$");
        String expectedPasswordError = messageSource.getMessage("password.size", passwordLength, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "pl");
    }
    @Test
    void signUp_passwordTooLongPLLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), defaultAuthInputDto.password().repeat(64));
        String expectedPasswordError = messageSource.getMessage("password.size", new Object[]{8, 64}, new Locale("pl"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "pl");
    }

    @Test
    void signUp_emailBlankENLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto("", defaultAuthInputDto.password());
        String expectedErrorMessage = messageSource.getMessage("login.notBlank", null, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedErrorMessage, "en");
    }
    @Test
    void signUp_invalidEmailENLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto("user", defaultAuthInputDto.password());
        String expectedErrorMessage = messageSource.getMessage("login.email", null, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedErrorMessage, "en");
    }

    @Test
    void signUp_invalidPasswordPatternENLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "password123");
        String expectedPasswordError = messageSource.getMessage("password.pattern", null, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "en");
    }
    @Test
    void signUp_passwordBlankENLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), " ");
        String expectedPasswordError = messageSource.getMessage("password.notBlank", null, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "en");
    }
    @Test
    void signUp_passwordTooShortENLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), "Pas3$");
        String expectedPasswordError = messageSource.getMessage("password.size", passwordLength, new Locale("en"));
        performSignUpWithInvalidInput(invalidAuthInputDto, expectedPasswordError, "en");
    }
    @Test
    void signUp_passwordTooLongENLocale_badRequest() throws Exception {
        AuthInputDto invalidAuthInputDto = new AuthInputDto(defaultAuthInputDto.login(), defaultAuthInputDto.password().repeat(64));
        String expectedPasswordError = messageSource.getMessage("password.size", passwordLength, new Locale("en"));
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
    void signUp_userAlreadyExists_userEnabled_conflict() throws Exception {
        performSignUp_userAlreadyExists(true);
    }
    @Test
    void signUp_userAlreadyExists_userNotEnabled_ok() throws Exception {
        performSignUp_userAlreadyExists(false);
    }
    private void performSignUp_userAlreadyExists(
            Boolean enabled
    ) throws Exception {
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        User user = (User) userRepository.findByLogin(defaultAuthInputDto.login());
        assertEquals(defaultAuthInputDto.login(), user.getUsername());

        if (enabled) userRepository.save(new User(user.getId(), user.getLogin(), user.getPassword(), true));
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(enabled ? status().isConflict() : status().isOk());
        UserDetails user1 = userRepository.findByLogin(defaultAuthInputDto.login());
        assertEquals(defaultAuthInputDto.login(), user1.getUsername());
    }

    @Test
    void verifyUser_EN_ok() throws Exception {
        verifyUser_ok_util(Locale.forLanguageTag("en-US"));
    }
    @Test
    void verifyUser_PL_ok() throws Exception {
        verifyUser_ok_util(Locale.forLanguageTag("pl-PL"));
    }

    private void verifyUser_ok_util(
            Locale locale
    ) throws Exception {
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale.getLanguage())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String token = "";
        String successMessage = messageSource.getMessage("emailConfirmation.success", null, locale);
        when(userVerificationService.verifyToken(token)).thenReturn(defaultAuthInputDto.login());
        mockMvc.perform(get(rootUrl + "confirm")
                        .queryParam("token", token)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale.getLanguage()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(successMessage)));
    }

    @Test
    void signIn_userVerifiedAndEnabled_ok() throws Exception {
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        User user = (User) userRepository.findByLogin(defaultAuthInputDto.login());
        assertFalse(user.isEnabled());

        String verificationToken = "";
        when(userVerificationService.verifyToken(verificationToken)).thenReturn(user.getLogin());
        mockMvc.perform(get(rootUrl+"confirm")
                .queryParam("token", ""))
                .andExpect(status().isOk());
        User verifiedUser = (User) userRepository.findByLogin(defaultAuthInputDto.login());
        assertTrue(verifiedUser.isEnabled());

        mockMvc.perform(post(rootUrl + "signin")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void signIn_userNotEnabled_unauthorized() throws Exception {
        mockMvc.perform(post(rootUrl + "signup")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        UserDetails user = userRepository.findByLogin(defaultAuthInputDto.login());
        assertEquals(defaultAuthInputDto.login(), user.getUsername());

        mockMvc.perform(post(rootUrl + "signin")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signIn_userDoesNotExist_unauthorized() throws Exception {
        mockMvc.perform(post(rootUrl + "signin")
                        .content(new ObjectMapper().writeValueAsBytes(defaultAuthInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        assertNull(userRepository.findByLogin(defaultAuthInputDto.login()));
    }

    @Test
    void performTokenRefresh_tokenNotValid_unauthorized() throws Exception {
        String invalidToken = "";
        RefreshJwtRequest refreshJwtRequest = new RefreshJwtRequest(invalidToken);
        when(tokenRefreshService.refreshToken(invalidToken)).thenThrow(ExpiredJwtException.class);

        mockMvc.perform(post(rootUrl+"refresh")
                .content(new ObjectMapper().writeValueAsBytes(refreshJwtRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void performTokenRefresh_tokenValid_ok() throws Exception {
        String invalidToken = "";
        RefreshJwtRequest refreshJwtRequest = new RefreshJwtRequest(invalidToken);
        when(tokenRefreshService.refreshToken(invalidToken)).thenReturn(new JwtResponse("", ""));

        mockMvc.perform(post(rootUrl+"refresh")
                        .content(new ObjectMapper().writeValueAsBytes(refreshJwtRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
