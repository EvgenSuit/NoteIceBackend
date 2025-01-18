package com.example.noteice.services.auth;

import com.example.noteice.dtos.User;
import com.example.noteice.security.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@EnableAsync
public class UserVerificationService {
    private final TokenProvider tokenProvider;
    private final MessageSource messageSource;
    private final JavaMailSender javaMailSender;

    @Value("${url.up}")
    private String rootUrl;

    public UserVerificationService(TokenProvider tokenProvider,
                                   MessageSource messageSource,
                                   JavaMailSender javaMailSender) {
        this.tokenProvider = tokenProvider;
        this.messageSource = messageSource;
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void confirmUser(User user, Locale locale) {
        String verificationToken = tokenProvider.generateVerificationToken(user.getLogin());
        String confirmationUrl = rootUrl + "noteice/auth/confirm?token=" + verificationToken;
        String subject = messageSource.getMessage("emailConfirmation.subject", null, locale);
        String body = messageSource.getMessage("emailConfirmation.body", null, locale);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getLogin());
        message.setSubject(subject);
        message.setText(body + "\n" + confirmationUrl);
        javaMailSender.send(message);
    }
    public String verifyToken(String token) {
        return tokenProvider.verifyToken(token);
    }
}
