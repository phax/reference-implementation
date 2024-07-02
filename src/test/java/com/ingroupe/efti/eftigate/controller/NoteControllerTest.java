package com.ingroupe.efti.eftigate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.NotesDto;
import com.ingroupe.efti.eftigate.service.ControlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetadataController.class)
@ContextConfiguration(classes= {NoteController.class})
@ExtendWith(SpringExtension.class)
class NoteControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    ControlService controlService;

    @Test
    @WithMockUser
    void createNoteTest() throws Exception {
        final NotesDto notesDto = new NotesDto();
        notesDto.setEFTIPlatformUrl("platform");
        notesDto.setEFTIDataUuid("uuid");
        notesDto.setEFTIGateUrl("gate");
        notesDto.setRequestUuid("requestUuid");
        notesDto.setNote("Conducteur suspect");
        Mockito.when(controlService.getControlByRequestUuid("requestUuid")).thenReturn(new ControlDto());

        mockMvc.perform(post("/v1/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsBytes(notesDto)))
                .andExpect(status().isAccepted());

        Mockito.verify(controlService).createNoteRequestForControl(notesDto);
    }
}
