package com.example.noteice.services;

import com.example.noteice.dtos.AuthInputDto;
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

    public void signUp(AuthInputDto authInputDto) {
        if (userRepository.findByLogin(authInputDto.login()) == null) {
            String encodedPassword = new BCryptPasswordEncoder().encode(authInputDto.password());
            userRepository.save(new User(null, authInputDto.login(), encodedPassword));
        } else throw new UserAlreadyExistsException("User with this login already exists");
    }
}
