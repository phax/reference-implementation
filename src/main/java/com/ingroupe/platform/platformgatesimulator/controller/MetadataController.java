package com.ingroupe.platform.platformgatesimulator.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metadata")
@AllArgsConstructor
@Slf4j
public class MetadataController {

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMetadata(String metadataDto) {
        return new ResponseEntity<>(metadataDto, HttpStatus.OK);
    }
}
