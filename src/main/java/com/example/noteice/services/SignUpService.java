package com.example.noteice.services;

import com.example.noteice.dtos.SignUpDto;
import com.example.noteice.dtos.User;
import com.example.noteice.repositories.UserRepository;
import com.example.noteice.utils.UserAlreadyExistsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignUpService {
    private final UserRepository userRepository;

    public SignUpService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void signUp(SignUpDto signUpDto) throws UserAlreadyExistsException {
        if (userRepository.findByLogin(signUpDto.login()) == null) {
            String encodedPassword = new BCryptPasswordEncoder().encode(signUpDto.password());
            userRepository.save(new User(null, signUpDto.login(), encodedPassword));
        } else throw new UserAlreadyExistsException("User with login: " + signUpDto.login() + " already exists");
    }
}
