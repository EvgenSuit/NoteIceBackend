package com.example.noteice.controllers;

import com.example.noteice.dtos.Note;
import com.example.noteice.dtos.NoteRequestDto;
import com.example.noteice.dtos.NoteResponseDto;
import com.example.noteice.dtos.NoteResponseExtension;
import com.example.noteice.repositories.NotesRepository;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Array;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/noteice/notes")
public class NotesController {
    private final NotesRepository notesRepository;

    public NotesController(NotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    @PostMapping("/")
    public ResponseEntity<Void> postNote(
            @RequestBody NoteRequestDto noteRequestDto,
            Principal principal
    ) {
        val savedNote = notesRepository.save(new Note(null, noteRequestDto.title(), noteRequestDto.content(), principal.getName()));
        val uri = UriComponentsBuilder.fromPath("/noteice/notes/{id}").buildAndExpand(savedNote.getId())
                .toUri();
        val header = new HttpHeaders();
        header.setLocation(uri);
        return new ResponseEntity<>(header, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDto> getNote(
            @PathVariable Long id,
            Principal principal
    ) {
        val login = principal.getName();
        val exists = notesRepository.existsByIdAndOwner(id, login);
        if (exists) {
            val extractedNote = NoteResponseExtension.getNoteResponseDto(notesRepository.findNoteByIdAndOwner(id, login));
            return ResponseEntity.ok(extractedNote);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/")
    public ResponseEntity<List<NoteResponseDto>> getAllNotes(
            Principal principal
    ) {
        val login = principal.getName();
        val notes = notesRepository.getAllByOwner(login);
        if (notes != null) {
            return ResponseEntity.ok(StreamSupport.stream(notes.spliterator(), false)
                    .map(NoteResponseExtension::getNoteResponseDto)
                    .collect(Collectors.toList()));
        }
        return ResponseEntity.notFound().build();
    }
}
