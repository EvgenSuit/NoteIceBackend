package com.example.noteice.services.auth;

import com.example.noteice.dtos.AuthInputDto;
import com.example.noteice.dtos.User;
import com.example.noteice.repositories.UserRepository;
import com.example.noteice.utils.UserAlreadyExistsException;
import lombok.val;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class SignUpService {
    private final UserRepository userRepository;
    private final UserVerificationService userVerificationService;

    public SignUpService(UserRepository userRepository,
                         UserVerificationService userVerificationService) {
        this.userRepository = userRepository;
        this.userVerificationService = userVerificationService;
    }

    public void signUp(AuthInputDto authInputDto,
                       Locale locale) {
        User savedUser = (User) userRepository.findByLogin(authInputDto.login());
        String encodedPassword = new BCryptPasswordEncoder().encode(authInputDto.password());
        if (savedUser == null) {
            User user = new User(null, authInputDto.login(), encodedPassword);
            userRepository.save(user);
            userVerificationService.confirmUser(user, locale);
        } else if (!savedUser.isEnabled()) {
            // resend verification email if a user exists but is not enabled
            User modifiedUser = new User(savedUser.getId(), savedUser.getLogin(), encodedPassword);
            userRepository.save(modifiedUser);
            userVerificationService.confirmUser(modifiedUser, locale);
        }
        else throw new UserAlreadyExistsException();
    }

    public void verifyToken(String token) {
        String login = userVerificationService.verifyToken(token);
        User currUser = (User) userRepository.findByLogin(login);
        userRepository.save(
                new User(currUser.getId(), currUser.getLogin(), currUser.getPassword(), true)
        );
    }

}
