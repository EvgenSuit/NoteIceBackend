package com.example.noteice.auth;

import com.example.noteice.dtos.NoteRequestDto;
import com.example.noteice.dtos.NoteResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NotesTests {

    @Autowired
    MockMvc mockMvc;

    private final String rootNotesUrl = "/noteice/notes/";

    @Test
    @DirtiesContext
    void postNote_notAuthenticated_shouldReturn403() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        mockMvc.perform(post(rootNotesUrl)
                .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DirtiesContext
    @WithMockUser
    void postNote_authentication_shouldPostNote() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        MvcResult mvcResult =  mockMvc.perform(post(rootNotesUrl)
                .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        String location = mvcResult.getResponse().getHeader(HttpHeaders.LOCATION);
        assertEquals(rootNotesUrl + "1", location);
    }

    @Test
    void getNotes_notAuthenticated_shouldReturn403() throws Exception {
        mockMvc.perform(get(rootNotesUrl))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser
    @DirtiesContext
    void getNotes_authenticated_shouldReturnNotes() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        NoteRequestDto noteRequestDto2 = new NoteRequestDto("title1", "content1");
        mockMvc.perform(post(rootNotesUrl)
                .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(post(rootNotesUrl)
                        .content(new ObjectMapper().writeValueAsBytes(noteRequestDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

         MvcResult mvcResult = mockMvc.perform(get(rootNotesUrl))
                .andExpect(status().isOk())
                .andReturn();
         List<NoteResponseDto> returnedResponseDtos = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), new TypeReference<List<NoteResponseDto>>() {});
         assertEquals(2, returnedResponseDtos.size());

         NoteResponseDto noteResponseDto1 = returnedResponseDtos.get(0);
         assertEquals(noteRequestDto.title(), noteResponseDto1.getTitle());
         assertEquals(noteRequestDto.content(), noteResponseDto1.getContent());

        NoteResponseDto noteResponseDto2 = returnedResponseDtos.get(1);
        assertEquals(noteRequestDto2.title(), noteResponseDto2.getTitle());
        assertEquals(noteRequestDto2.content(), noteResponseDto2.getContent());
    }

    @Test
    @DirtiesContext
    void getNote_notAuthenticated_shouldReturn403() throws Exception {
        mockMvc.perform(get(rootNotesUrl + "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void getNote_authenticated_shouldReturnNote() throws Exception {
        NoteRequestDto noteRequestDto = new NoteRequestDto("title", "content");
        mockMvc.perform(post(rootNotesUrl)
                        .content(new ObjectMapper().writeValueAsBytes(noteRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        MvcResult mvcResult = mockMvc.perform(get(rootNotesUrl + "1"))
                .andExpect(status().isOk())
                .andReturn();
        NoteResponseDto noteResponseDto = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), new TypeReference<NoteResponseDto>() {});
        assertEquals(noteRequestDto.title(), noteResponseDto.getTitle());
        assertEquals(noteRequestDto.content(), noteResponseDto.getContent());
    }
    @Test
    @WithMockUser
    @DirtiesContext
    void getNote_authenticated_noteDoesNotExist_shouldReturn404() throws Exception {
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
