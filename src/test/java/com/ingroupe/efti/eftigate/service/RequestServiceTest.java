package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.utils.RequestStatusEnum;
import com.ingroupe.efti.eftigate.utils.RequestTypeEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RequestServiceTest extends AbstractServceTest {

    AutoCloseable openMocks;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestSendingService requestSendingService;
    private GateProperties gateProperties;
    private RequestService requestService;

    private final UilDto uilDto = new UilDto();
    private final ControlDto controlDto = new ControlDto();
    private final ControlEntity controlEntity = new ControlEntity();
    private final RequestEntity requestEntity = new RequestEntity();
    private final RequestDto requestDto = new RequestDto();

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);

        gateProperties = GateProperties.builder().ap(GateProperties.ApConfig.builder().url("url").password("pwd").username("usr").build()).build();
        requestService = new RequestService(requestRepository, requestSendingService, gateProperties, mapperUtils, objectMapper);

        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        String requestUuid = UUID.randomUUID().toString();

        this.uilDto.setGate("gate");
        this.uilDto.setUuid("uuid");
        this.uilDto.setPlatform("plateform");
        this.controlDto.setEftiDataUuid(uilDto.getUuid());
        this.controlDto.setEftiGateUrl(uilDto.getGate());
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
        this.controlEntity.setEftiGateUrl(controlDto.getEftiGateUrl());
        this.controlEntity.setSubsetEuRequested(controlDto.getSubsetEuRequested());
        this.controlEntity.setSubsetMsRequested(controlDto.getSubsetMsRequested());
        this.controlEntity.setCreatedDate(controlDto.getCreatedDate());
        this.controlEntity.setLastModifiedDate(controlDto.getLastModifiedDate());
        this.controlEntity.setEftiData(controlDto.getEftiData());
        this.controlEntity.setTransportMetadata(controlDto.getTransportMetaData());
        this.controlEntity.setFromGateUrl(controlDto.getFromGateUrl());

        this.requestDto.setStatus(RequestStatusEnum.RECEIVED.toString());
        this.requestDto.setRetry(0);
        this.requestDto.setCreatedDate(localDateTime);
        this.requestDto.setGateUrlDest(controlEntity.getEftiGateUrl());
        this.requestDto.setControl(ControlDto.builder().id(1).build());

        this.requestEntity.setStatus(this.requestDto.getStatus());
        this.requestEntity.setRetry(this.requestDto.getRetry());
        this.requestEntity.setCreatedDate(this.requestEntity.getCreatedDate());
        this.requestEntity.setGateUrlDest(this.requestDto.getGateUrlDest());
        this.requestEntity.setControl(controlEntity);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void createRequestEntityTest() throws SendRequestException, InterruptedException {
        final String edeliveryId = "id123";
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(requestSendingService.sendRequest(any())).thenReturn(edeliveryId);

        final RequestDto requestDto = requestService.createAndSendRequest(controlDto);

        Thread.sleep(1000);
        Mockito.verify(requestRepository, Mockito.times(2)).save(any());
        Assertions.assertNotNull(requestDto);
        Assertions.assertEquals(RequestStatusEnum.IN_PROGRESS.name(), requestDto.getStatus());
        Assertions.assertEquals(edeliveryId, requestDto.getEdeliveryMessageId());
    }

    @Test
    void shouldSetSendErrorTest() throws SendRequestException, InterruptedException {
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(requestSendingService.sendRequest(any())).thenThrow(SendRequestException.class);

        final RequestDto requestDto = requestService.createAndSendRequest(controlDto);

        Thread.sleep(1000);
        Mockito.verify(requestRepository, Mockito.times(2)).save(any());
        Assertions.assertNotNull(requestDto);
        Assertions.assertEquals(RequestStatusEnum.SEND_ERROR.name(), requestDto.getStatus());
        Assertions.assertNotNull(requestDto.getError());
    }

    @Test
    void shouldSetErrorTest() throws JsonProcessingException {
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        final RequestDto requestDto = requestService.createAndSendRequest(controlDto);

        Mockito.verify(requestRepository, Mockito.times(2)).save(any());
        Assertions.assertNotNull(requestDto);
        Assertions.assertEquals(RequestStatusEnum.SEND_ERROR.name(), requestDto.getStatus());
        Assertions.assertNotNull(requestDto.getError());
    }
}
