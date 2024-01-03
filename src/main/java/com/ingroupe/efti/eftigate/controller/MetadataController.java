package com.ingroupe.efti.eftigate.controller;

import com.ingroupe.efti.eftigate.controller.api.MetadataControllerApi;
import com.ingroupe.efti.eftigate.dto.MetadataRequestDto;
import com.ingroupe.efti.eftigate.dto.MetadataResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
@Slf4j
public class MetadataController implements MetadataControllerApi {

    @Override
    public MetadataResponseDto getMetadata(final @RequestBody MetadataRequestDto metadataRequestDto) {
        return MetadataResponseDto.builder().build();
    }
}
