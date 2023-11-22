package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.utils.RequestStatusEnum;
import com.ingroupe.efti.eftigate.utils.RequestTypeEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

class RequestServiceTest {

    private final RequestRepository requestRepository = Mockito.mock(RequestRepository.class);;

    private final MapperUtils mapperUtils = Mockito.mock(MapperUtils.class);

    private final RequestService requestService = new RequestService(requestRepository, mapperUtils);

    private final UilDto uilDto = new UilDto();
    private final ControlDto controlDto = new ControlDto();
    private final ControlEntity controlEntity = new ControlEntity();

    private final RequestEntity requestEntity = new RequestEntity();

    private final RequestDto requestDto = new RequestDto();

    @BeforeEach
    public void before() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String requestUuid = UUID.randomUUID().toString();

        this.uilDto.setGate("gate");
        this.uilDto.setUuid("uuid");
        this.uilDto.setPlatform("plateform");
        this.controlDto.setEftiDataUuid(uilDto.getUuid());
        this.controlDto.setEftigateurl(uilDto.getGate());
        this.controlDto.setEftiPlatformUrl(uilDto.getPlatform());
        this.controlDto.setRequestUuid(requestUuid);
        this.controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH.toString());
        this.controlDto.setStatus(StatusEnum.PENDING.toString());
        this.controlDto.setSubsetEuRequested("oki");
        this.controlDto.setSubsetMsRequested("oki");
        this.controlDto.setCreatedDate(localDateTime);
        this.controlDto.setLastModifiedDate(localDateTime);

        this.controlEntity.setEftiDataUuid(controlDto.getEftiDataUuid());
        this.controlEntity.setRequestUuid(controlDto.getRequestUuid());
        this.controlEntity.setRequestType(controlDto.getRequestType());
        this.controlEntity.setStatus(controlDto.getStatus());
        this.controlEntity.setEftiPlatformUrl(controlDto.getEftiPlatformUrl());
        this.controlEntity.setEftiGateUrl(controlDto.getEftigateurl());
        this.controlEntity.setSubsetEuRequested(controlDto.getSubsetEuRequested());
        this.controlEntity.setSubsetMsRequested(controlDto.getSubsetMsRequested());
        this.controlEntity.setCreatedDate(controlDto.getCreatedDate());
        this.controlEntity.setLastModifiedDate(controlDto.getLastModifiedDate());
        this.controlEntity.setEftiData(controlDto.getEftiData());
        this.controlEntity.setTransportMetadata(controlDto.getTransportMetaData());
        this.controlEntity.setFromGateUrl(controlDto.getFromGateUrl());

        this.requestDto.setControlId(this.controlEntity.getId());
        this.requestDto.setStatus(RequestStatusEnum.RECEIVED.toString());
        this.requestDto.setRetry(0);
        this.requestDto.setCreatedDate(localDateTime);
        this.requestDto.setGateUrlDest(controlEntity.getEftiGateUrl());

        this.requestEntity.setControlId(this.requestDto.getControlId());
        this.requestEntity.setStatus(this.requestDto.getStatus());
        this.requestEntity.setRetry(this.requestDto.getRetry());
        this.requestEntity.setCreatedDate(this.requestEntity.getCreatedDate());
        this.requestEntity.setGateUrlDest(this.requestDto.getGateUrlDest());
    }

    @Test
    void createRequestEntityTest() {
        Mockito.when(mapperUtils.requestDtoToRequestEntity(requestDto)).thenReturn(requestEntity);
        Mockito.when(requestRepository.save(any())).thenReturn(requestEntity);

        RequestEntity requestEntityResult = requestService.createRequestEntity(controlEntity);

        Assertions.assertNotNull(requestEntityResult);
        Mockito.verify(mapperUtils, Mockito.times(1)).requestDtoToRequestEntity(any());
        Mockito.verify(requestRepository, Mockito.times(1)).save(any());
    }
}
