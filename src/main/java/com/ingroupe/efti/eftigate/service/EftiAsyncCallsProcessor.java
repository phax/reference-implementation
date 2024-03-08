package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor()
@Slf4j
public class EftiAsyncCallsProcessor {
    private final MetadataSearchRequestService defaultMetadataSearchRequestService;
    private final MetadataService metadataService;
    @Async
    @Transactional("metadataTransactionManager")
    public void checkLocalRepoAsync(final MetadataRequestDto metadataRequestDto, ControlDto saveControl) {
        List<MetadataDto> metadataDtoList = metadataService.search(metadataRequestDto);
        if (CollectionUtils.isNotEmpty(metadataDtoList)){
            defaultMetadataSearchRequestService.createRequest(saveControl, RequestStatusEnum.SUCCESS.name(), metadataDtoList);
        }
        else {
            defaultMetadataSearchRequestService.createRequest(saveControl, RequestStatusEnum.ERROR.name(), null);
        }
    }
}
