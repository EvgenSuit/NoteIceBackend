package com.example.noteice.repositories;

import com.example.noteice.dtos.Note;
import org.springframework.data.repository.CrudRepository;

public interface NotesRepository extends CrudRepository<Note, Long> {
    Note findNoteByIdAndOwner(Long id, String owner);
    boolean existsByIdAndOwner(Long id, String owner);

    Iterable<Note> getAllByOwner(String owner);
}
