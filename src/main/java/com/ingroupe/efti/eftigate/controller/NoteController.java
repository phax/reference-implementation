package com.ingroupe.efti.eftigate.controller;

import com.ingroupe.efti.eftigate.controller.api.NoteControllerApi;
import com.ingroupe.efti.eftigate.dto.NotesDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.service.ControlService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
@Slf4j
public class NoteController implements NoteControllerApi {

    private final ControlService controlService;

    @Override
    public ResponseEntity<String> createNote(final @RequestBody NotesDto notesDto) {
        log.info("POST on /notes with param requestUuid {}", notesDto.getRequestUuid());
        log.info("sending note to platform {}", notesDto.getEFTIPlatformUrl());
        final RequestUuidDto requestUuidDto = controlService.createNoteRequestForControl(notesDto);
        if (requestUuidDto != null && StringUtils.isNotBlank(requestUuidDto.getErrorCode())) {
            return new ResponseEntity<>("Error encountered, note was not sent", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Note sent", HttpStatus.ACCEPTED);
    }
}
