package com.example.noteice.dtos;

import com.example.noteice.validators.auth.password.PasswordValidatorInterface;
import jakarta.validation.constraints.*;

public record AuthInputDto(
        @NotBlank(message = "{login.notBlank}")
        @Email(message = "{login.email}")
        String login,

        // use custom password validator since jakarta annotations don't strictly follow the order
        // (e.g. when @NotBlank, @Size and @Pattern annotations are applied, and a password is blank or empty, any annotation other than @NotBlank is applied
        @PasswordValidatorInterface
        String password
) {
}
