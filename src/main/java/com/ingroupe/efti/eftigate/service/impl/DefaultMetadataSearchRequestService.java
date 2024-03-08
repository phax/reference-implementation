package com.ingroupe.efti.eftigate.service.impl;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.MetadataSearchRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Service
public class DefaultMetadataSearchRequestService implements MetadataSearchRequestService {

    private final RequestRepository requestRepository;
    private final MapperUtils mapperUtils;

    @Override
    public void createRequest(ControlDto controlDto, String status, List<MetadataDto> metadataDtoList) {
        RequestDto requestDto = RequestDto.builder()
                .createdDate(LocalDateTime.now(ZoneOffset.UTC))
                .retry(0)
                .control(controlDto)
                .status(status)
                .build();
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        RequestEntity savedRequest = requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto));
        savedRequest.setMetadataResults(buildMetadataResult(metadataDtoList));
        requestRepository.save(savedRequest);
    }
    @Override
    public boolean allRequestsContainsData(List<RequestEntity> controlEntityRequests) {
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> Objects.nonNull(requestEntity.getMetadataResults()));
    }

    @Override
    public void setDataFromRequests(final ControlEntity controlEntity) {
        List<MetadataResult> metadataResultList = controlEntity.getRequests().stream()
                .flatMap(s -> s.getMetadataResults().getMetadataResult().stream())
                .toList();
        controlEntity.setMetadataResults(new MetadataResults(metadataResultList));
    }

    private MetadataResults buildMetadataResult(List<MetadataDto> metadataDtos) {
        List<MetadataResult> metadataResultList = mapperUtils.metadataDtosToMetadataEntities(metadataDtos);
        return MetadataResults.builder()
                .metadataResult(metadataResultList)
                .build();
    }
}
