package com.example.noteice.dtos.note;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequestDto(
        @NotBlank(message = "{noteTitle.notBlank}")
        @Size(min = 1, max = 80,
        message = "{noteTitle.wrongSize}")
        String title,

        @NotBlank(message = "{noteContent.notBlank}")
        @Size(min = 1, max = 1000,
        message = "{noteContent.wrongSize}")
        String content
) {
}
