package com.example.noteice.notes;

import com.example.noteice.dtos.note.Note;
import com.example.noteice.dtos.note.NoteDeleteIds;
import com.example.noteice.dtos.note.NoteRequestDto;
import com.example.noteice.repositories.NotesRepository;
import com.example.noteice.services.notes.NotesService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jdk.jfr.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class NotesControllerTests {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    MessageSource messageSource;
    @Autowired
    NotesRepository notesRepository;
    @Autowired
    NotesService notesService;

    private final ObjectMapper objectMapper;

    NotesControllerTests() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private final Integer MAX_TITLE_LENGTH = 80;
    private final Integer MAX_CONTENT_LENGTH = 1000;

    private final String rootNotesUrl = "/noteice/notes/";

    @Test
    @WithMockUser
    void deleteNotes_notesDoNotExist_success_noNotesRemoved() throws Exception {
        mockMvc.perform(delete(rootNotesUrl+"delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(new NoteDeleteIds(List.of(1L, 2L)))))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "user")
    void deleteNotes_notesExist_success_notesRemoved() throws Exception {
        NoteRequestDto noteRequestDto1 = new NoteRequestDto("title", "content");
        NoteRequestDto noteRequestDto2 = new NoteRequestDto("title", "content");
        mockMvc.perform(post(rootNotesUrl)
                        .content(new ObjectMapper().writeValueAsBytes(noteRequestDto1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(post(rootNotesUrl)
                        .content(new ObjectMapper().writeValueAsBytes(noteRequestDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertNotNull(notesRepository.findNoteByIdAndOwner(1L, "user"));
        assertNotNull(notesRepository.findNoteByIdAndOwner(2L, "user"));

        mockMvc.perform(delete(rootNotesUrl+"delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsBytes(new NoteDeleteIds(List.of(1L, 2L)))))
                .andExpect(status().isOk());

        assertNull(notesRepository.findNoteByIdAndOwner(1L, "user"));
        assertNull(notesRepository.findNoteByIdAndOwner(2L, "user"));
    }

    @Test
    @WithMockUser(username = "user")
    void editNote_noteAlreadyExists_noteModified() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        notesService.saveNote(noteRequestDto, "user");

        MvcResult mvcResult = mockMvc.perform(put(rootNotesUrl+"1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(new NoteRequestDto("new title", "new content"))))
                .andExpect(status().isOk())
                .andReturn();
        Note modifiedNote = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), new TypeReference<>() {});
        assertNotNull(modifiedNote);
    }

    @Test
    void postNote_notAuthenticated_forbidden() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        mockMvc.perform(post(rootNotesUrl)
                .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "username")
    void postNote_authenticated_created() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        MvcResult mvcResult = mockMvc.perform(post(rootNotesUrl)
                .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        Note returnedNote = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Note>() {});
        Note actualNote = notesRepository.findNoteByIdAndOwner(1L, "username");

        assertEquals(actualNote.getId(), returnedNote.getId());
        assertEquals(actualNote.getTitle(), returnedNote.getTitle());
        assertEquals(actualNote.getContent(), returnedNote.getContent());
        assertEquals(actualNote.getCreatedAt().truncatedTo(ChronoUnit.MICROS), actualNote.getCreatedAt().truncatedTo(ChronoUnit.MICROS));
        assertEquals(actualNote.getLastModifiedAt().truncatedTo(ChronoUnit.MICROS), actualNote.getLastModifiedAt().truncatedTo(ChronoUnit.MICROS));
    }
    @Test
    @WithMockUser
    void postNote_authenticated_lengthExceeded_badRequest_USLocale() throws Exception {
        postNote_lengthExceeded_util(Locale.forLanguageTag("en-US"));
    }
    @Test
    @WithMockUser
    void postNote_authenticated_lengthExceeded_badRequest_PLLocale() throws Exception {
        postNote_lengthExceeded_util(Locale.forLanguageTag("pl-PL"));
    }
    private void postNote_lengthExceeded_util(
            Locale locale
    ) throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("t".repeat(800),
                "c".repeat(4000));
        String titleLengthErrorMessage = messageSource.getMessage("noteTitle.wrongSize",
                null, locale).replace("{max}", MAX_TITLE_LENGTH.toString());

        String contentLengthErrorMessage = messageSource.getMessage("noteContent.wrongSize",
                null, locale).replace("{max}", MAX_CONTENT_LENGTH.toString());
        mockMvc.perform(post(rootNotesUrl)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale.getLanguage())
                        .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(titleLengthErrorMessage)))
                .andExpect(content().string(containsString(contentLengthErrorMessage)))
                .andReturn();
    }


    @Test
    void getNotes_notAuthenticated_forbidden() throws Exception {
        mockMvc.perform(get(rootNotesUrl))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser
    void getNotes_authenticated_ok() throws Exception {
        NoteRequestDto noteRequestDto1 = new NoteRequestDto("title", "content");
        NoteRequestDto noteRequestDto2 = new NoteRequestDto("title1", "content1");
        mockMvc.perform(post(rootNotesUrl)
                .content(new ObjectMapper().writeValueAsBytes(noteRequestDto1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(post(rootNotesUrl)
                        .content(new ObjectMapper().writeValueAsBytes(noteRequestDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

         MvcResult mvcResult = mockMvc.perform(get(rootNotesUrl))
                .andExpect(status().isOk())
                .andReturn();
         List<Note> returnedNotes = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), new TypeReference<>() {});
         assertEquals(2, returnedNotes.size());
         System.out.println(returnedNotes);

         // since notes are sorted in desc order, the first inserted note gets positioned as the second
        Note note1 = returnedNotes.get(1);
         assertEquals(noteRequestDto1.title(), note1.getTitle());
         assertEquals(noteRequestDto1.content(), note1.getContent());
         assertEquals(note1.getCreatedAt(), note1.getLastModifiedAt());

        Note note2 = returnedNotes.get(0);
        assertEquals(noteRequestDto2.title(), note2.getTitle());
        assertEquals(noteRequestDto2.content(), note2.getContent());
        assertEquals(note2.getCreatedAt(), note2.getLastModifiedAt());

    }

    @Test
    void getNote_notAuthenticated_forbidden() throws Exception {
        mockMvc.perform(get(rootNotesUrl + "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getNote_authenticated_ok() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        mockMvc.perform(post(rootNotesUrl)
                        .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        MvcResult mvcResult = mockMvc.perform(get(rootNotesUrl + "1"))
                .andExpect(status().isOk())
                .andReturn();
        Note returnedNote = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), new TypeReference<>() {});
        assertEquals(noteRequestDto.title(), returnedNote.getTitle());
        assertEquals(noteRequestDto.content(), returnedNote.getContent());
        assertEquals(returnedNote.getCreatedAt(), returnedNote.getLastModifiedAt());
    }
    @Test
    @WithMockUser
    void getNote_authenticated_noteDoesNotExist_notFound() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        mockMvc.perform(post(rootNotesUrl)
                        .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(get(rootNotesUrl + "2"))
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
