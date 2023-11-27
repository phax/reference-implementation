package com.ingroupe.efti.eftigate.service;


import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.utils.RequestTypeEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

class ControlServiceTest {

    private final ControlRepository controlRepository = Mockito.mock(ControlRepository.class);

    private final MapperUtils mapperUtils = Mockito.mock(MapperUtils.class);

    private final RequestService requestService = Mockito.mock(RequestService.class);

    private final ControlService controlService = new ControlService(controlRepository, mapperUtils, requestService);

    private final UilDto uilDto = new UilDto();
    private final ControlDto controlDto = new ControlDto();
    private final ControlEntity controlEntity = new ControlEntity();
    private final RequestUuidDto requestUuidDto = new RequestUuidDto();
    private final String requestUuid = UUID.randomUUID().toString();

    @BeforeEach
    public void before() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String status = StatusEnum.PENDING.toString();

        requestUuidDto.setRequestUuid(requestUuid);
        requestUuidDto.setStatus(status);

        this.uilDto.setGate("gate");
        this.uilDto.setUuid("uuid");
        this.uilDto.setPlatform("plateform");
        this.controlDto.setEftiDataUuid(uilDto.getUuid());
        this.controlDto.setEftiGateUrl(uilDto.getGate());
        this.controlDto.setEftiPlatformUrl(uilDto.getPlatform());
        this.controlDto.setRequestUuid(requestUuid);
        this.controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH.toString());
        this.controlDto.setStatus(status);
        this.controlDto.setSubsetEuRequested("oki");
        this.controlDto.setSubsetMsRequested("oki");
        this.controlDto.setCreatedDate(localDateTime);
        this.controlDto.setLastModifiedDate(localDateTime);

        this.controlEntity.setEftiDataUuid(controlDto.getEftiDataUuid());
        this.controlEntity.setRequestUuid(controlDto.getRequestUuid());
        this.controlEntity.setRequestType(controlDto.getRequestType());
        this.controlEntity.setStatus(controlDto.getStatus());
        this.controlEntity.setEftiPlatformUrl(controlDto.getEftiPlatformUrl());
        this.controlEntity.setEftiGateUrl(controlDto.getEftiGateUrl());
        this.controlEntity.setSubsetEuRequested(controlDto.getSubsetEuRequested());
        this.controlEntity.setSubsetMsRequested(controlDto.getSubsetMsRequested());
        this.controlEntity.setCreatedDate(controlDto.getCreatedDate());
        this.controlEntity.setLastModifiedDate(controlDto.getLastModifiedDate());
        this.controlEntity.setEftiData(controlDto.getEftiData());
        this.controlEntity.setTransportMetadata(controlDto.getTransportMetaData());
        this.controlEntity.setFromGateUrl(controlDto.getFromGateUrl());
    }

    @Test
    void getByIdWithDataTest() {
        Mockito.when(controlRepository.findById(1L)).thenReturn(Optional.of(new ControlEntity()));

        ControlEntity controlEntity = controlService.getById(1L);

        Mockito.verify(controlRepository, Mockito.times(1)).findById(1L);
        Assertions.assertNotNull(controlEntity);
    }

    @Test
    void createControlEntityTest() {
        Mockito.when(mapperUtils.controlDtoToControEntity(controlDto)).thenReturn(controlEntity);
        Mockito.when(controlRepository.save(any())).thenReturn(controlEntity);

        ControlEntity controlEntityResult = controlService.createControlEntity(uilDto);

        Mockito.verify(mapperUtils, Mockito.times(1)).controlDtoToControEntity(any());
        Mockito.verify(controlRepository, Mockito.times(1)).save(any());
        Assertions.assertNotNull(controlEntityResult);
    }

    @Test
    void getControlEntitySucessTest() {
        Mockito.when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));

        RequestUuidDto requestUuidDtoResult = controlService.getControlEntity(requestUuid);

        Mockito.verify(controlRepository, Mockito.times(1)).findByRequestUuid(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        Assertions.assertEquals(requestUuidDtoResult.getRequestUuid(), controlEntity.getRequestUuid());
    }

    @Test
    void getControlEntityNotFoundTest() {
        Mockito.when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.empty());

        RequestUuidDto requestUuidDtoResult = controlService.getControlEntity(requestUuid);

        Mockito.verify(controlRepository, Mockito.times(1)).findByRequestUuid(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        Assertions.assertEquals("ERROR", requestUuidDtoResult.getStatus());
        Assertions.assertNull(requestUuidDtoResult.getEFTIData());
    }
}
