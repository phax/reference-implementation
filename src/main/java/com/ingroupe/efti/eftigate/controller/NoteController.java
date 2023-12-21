package com.ingroupe.efti.eftigate.controller;

import com.ingroupe.efti.eftigate.controller.api.NoteControllerApi;
import com.ingroupe.efti.eftigate.dto.NotesDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class NoteController implements NoteControllerApi {

    @Override
    public void createNote(final @RequestBody NotesDto notesDto) {
        //to be done
    }
}
