package com.example.noteice.notes;

import com.example.noteice.dtos.note.Note;
import com.example.noteice.dtos.note.NoteEditResult;
import com.example.noteice.dtos.note.NoteRequestDto;
import com.example.noteice.repositories.NotesRepository;
import com.example.noteice.services.notes.NotesService;
import com.example.noteice.utils.notes.NoteNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotesServiceTests {
    private Clock clock;
    @Autowired
    NotesRepository notesRepository;
    private NotesService notesService;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        notesService = new NotesService(clock, notesRepository);
    }

    @Test
    void saveNote_success() {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        notesService.saveNote(noteRequestDto, "");

        Instant currTime = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        Note savedNote = notesService.getNote(1L, "");
        assertEquals(new Note(savedNote.getId(), noteRequestDto.title(), noteRequestDto.content(),
                "", currTime, currTime), new Note(
                savedNote.getId(), savedNote.getTitle(), savedNote.getContent(), savedNote.getOwner(),
                savedNote.getCreatedAt().truncatedTo(ChronoUnit.MILLIS),
                savedNote.getLastModifiedAt().truncatedTo(ChronoUnit.MILLIS)
        ));
    }

    @Test
    void editNote_noteExists_noteModified() {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        notesService.saveNote(noteRequestDto, "");

        NoteEditResult noteEditResult = notesService.editNote(1L, noteRequestDto, "");
        Note returnedNote = noteEditResult.note();
        assertEquals(1L, returnedNote.getId());
        assertEquals(noteRequestDto.title(), returnedNote.getTitle());
        assertEquals(noteRequestDto.content(), returnedNote.getContent());
        assertFalse(noteEditResult.isCreated());

        assertTrue(notesRepository.existsByIdAndOwner(1L, ""));
        // assert that a new note was not created
        assertEquals(1, StreamSupport.stream(notesRepository.getAllByOwner("").spliterator(), false).toList().size());
        assertFalse(notesRepository.existsByIdAndOwner(2L, ""));
    }
    @Test
    void editNote_noteDoesNotExist_noteCreated() {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");

        NoteEditResult noteEditResult = notesService.editNote(1L, noteRequestDto, "");
        Note returnedNote = noteEditResult.note();
        assertEquals(1L, returnedNote.getId());
        assertEquals(noteRequestDto.title(), returnedNote.getTitle());
        assertEquals(noteRequestDto.content(), returnedNote.getContent());
        assertTrue(noteEditResult.isCreated());

        assertTrue(notesRepository.existsByIdAndOwner(1L, ""));
    }

    @Test
    void getNote_doesNotExist() {
        assertThrows(NoteNotFoundException.class, () -> notesService.getNote(1L, ""));
    }

    @Test
    void deleteNotes_notesDoNotExist_success() {
        notesService.deleteNotes(List.of(1L), "");
    }
    @Test
    void deleteNotes_notesExist_success() {
        notesService.saveNote(new NoteRequestDto("title", "content"), "");
        notesService.deleteNotes(List.of(1L), "");
    }
}
