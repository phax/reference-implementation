package com.ingroupe.efti.eftigate.service;


import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.utils.RequestTypeEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

class ControlServiceTest {
    @Mock
    private ControlRepository controlRepository = Mockito.mock(ControlRepository.class);

    @Mock
    private MapperUtils mapperUtils = Mockito.mock(MapperUtils.class);

    @Mock
    private RequestService requestService = Mockito.mock(RequestService.class);

    @InjectMocks
    private ControlService controlService = new ControlService(controlRepository, mapperUtils, requestService);

    private final UilDto uilDto = new UilDto();
    private ControlDto controlDto = new ControlDto();
    private ControlEntity controlEntity = new ControlEntity();

    @BeforeEach
    public void before() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String requestUuid = UUID.randomUUID().toString();

        this.uilDto.setGate("gate");
        this.uilDto.setUuid("uuid");
        this.uilDto.setPlatform("plateform");
        this.controlDto.setEftidatauuid(uilDto.getUuid());
        this.controlDto.setEftigateurl(uilDto.getGate());
        this.controlDto.setEftiplatformurl(uilDto.getPlatform());
        this.controlDto.setRequestuuid(requestUuid);
        this.controlDto.setRequesttype(RequestTypeEnum.LOCAL_UIL_SEARCH.toString());
        this.controlDto.setStatus(StatusEnum.PENDING.toString());
        this.controlDto.setSubseteurequested("oki");
        this.controlDto.setSubsetmsrequested("oki");
        this.controlDto.setCreateddate(localDateTime);
        this.controlDto.setLastmodifieddate(localDateTime);

        this.controlEntity.setEftidatauuid(controlDto.getEftidatauuid());
        this.controlEntity.setRequestuuid(controlDto.getRequestuuid());
        this.controlEntity.setRequesttype(controlDto.getRequesttype());
        this.controlEntity.setStatus(controlDto.getStatus());
        this.controlEntity.setEftiplatformurl(controlDto.getEftiplatformurl());
        this.controlEntity.setEftigateurl(controlDto.getEftigateurl());
        this.controlEntity.setSubseteurequested(controlDto.getSubseteurequested());
        this.controlEntity.setSubsetmsrequested(controlDto.getSubsetmsrequested());
        this.controlEntity.setCreateddate(controlDto.getCreateddate());
        this.controlEntity.setLastmodifieddate(controlDto.getLastmodifieddate());
        this.controlEntity.setEftidata(controlDto.getEftidata());
        this.controlEntity.setTransportmetadata(controlDto.getTransportmetadata());
        this.controlEntity.setFromgateurl(controlDto.getFromgateurl());
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
}
