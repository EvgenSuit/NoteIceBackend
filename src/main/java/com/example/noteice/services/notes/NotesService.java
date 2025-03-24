package com.example.noteice.services.notes;

import com.example.noteice.dtos.note.Note;
import com.example.noteice.dtos.note.NoteEditResult;
import com.example.noteice.dtos.note.NoteRequestDto;
import com.example.noteice.repositories.NotesRepository;
import com.example.noteice.utils.notes.NoteNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class NotesService {
    private final Clock clock;
    private final NotesRepository notesRepository;

    public NotesService(Clock clock,
                        NotesRepository notesRepository) {
        this.clock = clock;
        this.notesRepository = notesRepository;
    }

    public NoteEditResult editNote(Long id, NoteRequestDto noteRequestDto, String principalName) {
        Note foundNote = notesRepository.findNoteByIdAndOwner(id, principalName);
        boolean exists = foundNote != null;
        Instant currTimeInstant = clock.instant();
        Instant createdAt = exists ? foundNote.getCreatedAt() : currTimeInstant;
        Note note = new Note(exists ? id : null, noteRequestDto.title(), noteRequestDto.content(), principalName, createdAt, currTimeInstant);
        return new NoteEditResult(notesRepository.save(note), !exists);
    }

    public Note saveNote(NoteRequestDto noteRequestDto, String principalName) {
        Instant currTimeInstant = clock.instant();
        Note note = new Note(null, noteRequestDto.title(), noteRequestDto.content(), principalName, currTimeInstant, currTimeInstant);
        return notesRepository.save(note);
    }

    public Note getNote(Long id, String principalName) {
        boolean exists = notesRepository.existsByIdAndOwner(id, principalName);
        if (exists) {
            return notesRepository.findNoteByIdAndOwner(id, principalName);
        } else throw new NoteNotFoundException();
    }

    public Iterable<Note> getAllNotes(String principalName) {
        return notesRepository.getAllByOwner(principalName);
    }

    public void deleteNotes(List<Long> ids, String principalName) {
        notesRepository.deleteAllByIdsAndOwner(ids, principalName);
    }
}
