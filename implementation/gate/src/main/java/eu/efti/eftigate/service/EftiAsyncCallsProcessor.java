package eu.efti.eftigate.service;

import eu.efti.commons.dto.MetadataDto;
import eu.efti.commons.dto.MetadataRequestDto;
import eu.efti.commons.enums.RequestStatusEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.eftigate.dto.ControlDto;
import eu.efti.eftigate.dto.RequestDto;
import eu.efti.eftigate.service.request.MetadataRequestService;
import eu.efti.metadataregistry.service.MetadataService;
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

    @Async
    public void checkLocalRepoAsync(final MetadataRequestDto metadataRequestDto, final ControlDto savedControl) {
        final List<MetadataDto> metadataDtoList = metadataService.search(metadataRequestDto);
        RequestDto request = metadataRequestService.createRequest(savedControl, metadataDtoList);
        if (shouldUpdateControl(savedControl, request, metadataDtoList)) {
            metadataRequestService.updateControlMetadata(request.getControl(), metadataDtoList);
        }
    }

    private static boolean shouldUpdateControl(ControlDto savedControl, RequestDto request, List<MetadataDto> metadataDtoList) {
        return request != null && RequestStatusEnum.SUCCESS.equals(request.getStatus())
                && CollectionUtils.isNotEmpty(metadataDtoList)
                && RequestTypeEnum.EXTERNAL_METADATA_SEARCH.equals(savedControl.getRequestType());
    }
}
