package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.ControlDto;
import com.ingroupe.efti.commons.dto.IdentifiersRequestDto;
import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.eftigate.service.request.MetadataRequestService;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor()
@Slf4j
public class EftiAsyncCallsProcessor {
    private final MetadataRequestService metadataRequestService;
    private final MetadataService metadataService;
    private final LogManager logManager;

    @Async
    public void checkLocalRepoAsync(final MetadataRequestDto metadataRequestDto, final ControlDto savedControl) {
        final List<MetadataDto> metadataDtoList = metadataService.search(metadataRequestDto);
        logManager.logLocalRegistryMessage(savedControl, metadataDtoList);
        final IdentifiersRequestDto request = metadataRequestService.createRequest(savedControl, RequestStatusEnum.SUCCESS, metadataDtoList);
        if (shouldUpdateControl(savedControl, request, metadataDtoList)) {
            metadataRequestService.updateControlMetadata(request.getControl(), metadataDtoList);
        }
    }

    private static boolean shouldUpdateControl(final ControlDto savedControl, final IdentifiersRequestDto request, final List<MetadataDto> metadataDtoList) {
        return request != null && RequestStatusEnum.SUCCESS.equals(request.getStatus())
                && CollectionUtils.isNotEmpty(metadataDtoList)
                && RequestTypeEnum.EXTERNAL_METADATA_SEARCH.equals(savedControl.getRequestType());
    }
}
