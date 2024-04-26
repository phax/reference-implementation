package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.service.request.MetadataRequestService;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor()
@Slf4j
public class EftiAsyncCallsProcessor {
    private final MetadataRequestService metadataRequestService;
    private final MetadataService metadataService;
    @Async
    @Transactional("metadataTransactionManager")
    public void checkLocalRepoAsync(final MetadataRequestDto metadataRequestDto, final ControlDto savedControl) {
        final List<MetadataDto> metadataDtoList = metadataService.search(metadataRequestDto);
        metadataRequestService.createRequest(savedControl, metadataDtoList);
    }
}
