package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Service
public class MetadataLocalRequestService {
    private final RequestRepository requestRepository;
    private final MapperUtils mapperUtils;
    public void createRequest(ControlDto controlDto, String status, List<MetadataDto> metadataDtoList) {
        RequestDto requestDto = RequestDto.builder()
                .createdDate(LocalDateTime.now(ZoneOffset.UTC))
                .retry(0)
                .control(controlDto)
                .status(status)
                .build();
        requestDto.setMetadataResults(buildMetadataResult(metadataDtoList));
        requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto));
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
    }

    public MetadataResults buildMetadataResult(List<MetadataDto> metadataDtos) {
        List<MetadataResult> metadataResultList = mapperUtils.metadataDtosToMetadataEntities(metadataDtos);
        return MetadataResults.builder()
                .metadataResult(metadataResultList)
                .build();
    }

}
