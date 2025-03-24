package com.example.noteice.dtos.note;

public record NoteEditResult(
        Note note,
        boolean isCreated
) {
}
