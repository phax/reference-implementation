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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

public class RequestServiceTest {

    @Mock
    private final RequestRepository requestRepository = Mockito.mock(RequestRepository.class);;

    @Mock
    private MapperUtils mapperUtils = Mockito.mock(MapperUtils.class);

    @InjectMocks
    RequestService requestService = new RequestService(requestRepository, mapperUtils);

    private final UilDto uilDto = new UilDto();
    private ControlDto controlDto = new ControlDto();
    private ControlEntity controlEntity = new ControlEntity();

    private RequestEntity requestEntity = new RequestEntity();

    private RequestDto requestDto = new RequestDto();

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

        this.requestDto.setControlid(this.controlEntity.getId());
        this.requestDto.setStatus(RequestStatusEnum.RECEIVED.toString());
        this.requestDto.setRetry(0);
        this.requestDto.setCreateddate(localDateTime);
        this.requestDto.setGateurldest(controlEntity.getEftigateurl());

        this.requestEntity.setControlid(this.requestDto.getControlid());
        this.requestEntity.setStatus(this.requestDto.getStatus());
        this.requestEntity.setRetry(this.requestDto.getRetry());
        this.requestEntity.setCreateddate(this.requestEntity.getCreateddate());
        this.requestEntity.setGateurldest(this.requestDto.getGateurldest());
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
