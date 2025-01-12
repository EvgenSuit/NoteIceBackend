package com.example.noteice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthInputDto(
        @NotBlank(message = "{login.notBlank}")
        @Email(message = "{login.email}")
        String login,

        @NotBlank(message = "{password.notBlank}")
        @Size(min = 8, max = 64, message = "{password.size}")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$",
                message = "{password.pattern}"
        )
        String password
) {
}
