package com.example.noteice.dtos;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NoteResponseExtension {
        public static NoteResponseDto getNoteResponseDto(Note note) {
                return new NoteResponseDto(note.getId(), note.getTitle(), note.getContent());
        }
}
