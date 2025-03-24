package com.example.noteice.repositories;

import com.example.noteice.dtos.note.Note;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotesRepository extends CrudRepository<Note, Long> {
    Note findNoteByIdAndOwner(Long id, String owner);
    boolean existsByIdAndOwner(Long id, String owner);

    Iterable<Note> getAllByOwner(String owner);

    @Modifying
    @Transactional
    @Query("DELETE FROM Note n WHERE n.id IN :ids AND n.owner = :owner")
    void deleteAllByIdsAndOwner(List<Long> ids, String owner);
}
