package com.ingroupe.platform.platformgatesimulator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.platform.platformgatesimulator.dto.UploadMetadataDto;
import com.ingroupe.platform.platformgatesimulator.service.ApIncomingService;
import com.ingroupe.platform.platformgatesimulator.service.ReaderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/metadata")
@AllArgsConstructor
@Slf4j
public class MetadataController {

    private final ApIncomingService apIncomingService;

    private final ReaderService readerService;

    @PostMapping("/upload")
    public ResponseEntity uploadMetadata(@RequestPart("data") MetadataDto metadataDto, @RequestPart("file") MultipartFile file) throws JsonProcessingException {
        log.info("/metadata/upload send");
        apIncomingService.uploadMetadata(metadataDto);
        readerService.uploadFile(file);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
