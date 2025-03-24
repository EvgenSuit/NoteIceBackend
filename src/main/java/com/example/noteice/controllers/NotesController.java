package com.example.noteice.controllers;

import com.example.noteice.dtos.note.Note;
import com.example.noteice.dtos.note.NoteDeleteIds;
import com.example.noteice.dtos.note.NoteEditResult;
import com.example.noteice.dtos.note.NoteRequestDto;
import com.example.noteice.services.notes.NotesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/noteice/notes")
public class NotesController {
    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    // TODO: when after deletion no notes exist, reset id to 0
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteNotes(
            @RequestBody NoteDeleteIds ids,
            Principal principal
    ) {
        notesService.deleteNotes(ids.ids(), principal.getName());
        return ResponseEntity.ok(null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> editNote(
            @PathVariable Long id,
            @RequestBody @Valid NoteRequestDto noteRequestDto,
            Principal principal
    ) {
        NoteEditResult noteEditResult = notesService.editNote(id, noteRequestDto, principal.getName());
        Note savedNote = noteEditResult.note();
        if (noteEditResult.isCreated()) {
            return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
        } else {
            return ResponseEntity.ok(savedNote);
        }
    }

    @PostMapping("/")
    public ResponseEntity<Note> postNote(
            @RequestBody @Valid NoteRequestDto noteRequestDto,
            Principal principal
    ) {
        Note savedNote = notesService.saveNote(noteRequestDto, principal.getName());
        return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNote(
            @PathVariable Long id,
            Principal principal
    ) {
        Note savedNote = notesService.getNote(id, principal.getName());
        return ResponseEntity.ok(savedNote);
    }

    @GetMapping("/")
    public ResponseEntity<List<Note>> getAllNotes(
            Principal principal
    ) {
        Iterable<Note> notes = notesService.getAllNotes(principal.getName());
        if (notes != null) {
            List<Note> notesList = StreamSupport.stream(notes.spliterator(), false)
                    // sort notes by lastModifiedAt in desc order
                    .sorted(Comparator.comparing(Note::getLastModifiedAt).reversed())
                    .toList();
            return ResponseEntity.ok(notesList);
        }
        return ResponseEntity.notFound().build();
    }

}
