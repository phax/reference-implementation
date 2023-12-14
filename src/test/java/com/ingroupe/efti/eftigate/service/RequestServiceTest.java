package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.dto.RetrieveMessageDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.utils.RequestStatusEnum;
import com.ingroupe.efti.eftigate.utils.RequestTypeEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        requestService = new RequestService(requestRepository, requestSendingService, gateProperties, mapperUtils, objectMapper, List.of(20,120,300));

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
        this.requestDto.setGateUrlDest("gate");

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
    void trySendDomibusSucessTest() throws SendRequestException {
        when(requestSendingService.sendRequest(any())).thenReturn("result");
        when(requestRepository.save(any())).thenReturn(requestEntity);

        requestService.sendRetryRequest(requestDto);
    }

    @Test
    void createRequestEntityTest() throws SendRequestException, InterruptedException {
        final String edeliveryId = "id123";
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(requestSendingService.sendRequest(any())).thenReturn(edeliveryId);

        final RequestDto requestDto = requestService.createAndSendRequest(controlDto);

        Thread.sleep(1000);
        Mockito.verify(requestRepository, Mockito.times(2)).save(any());
        assertNotNull(requestDto);
        assertEquals(RequestStatusEnum.IN_PROGRESS.name(), requestDto.getStatus());
        assertEquals(edeliveryId, requestDto.getEdeliveryMessageId());
    }

    @Test
    void shouldSetSendErrorTest() throws SendRequestException, InterruptedException {
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(requestSendingService.sendRequest(any())).thenThrow(SendRequestException.class);

        final RequestDto requestDto = requestService.createAndSendRequest(controlDto);

        Thread.sleep(1000);
        Mockito.verify(requestRepository, Mockito.times(2)).save(any());
        assertNotNull(requestDto);
        assertEquals(RequestStatusEnum.SEND_ERROR.name(), requestDto.getStatus());
        assertNotNull(requestDto.getError());
    }

    @Test
    void shouldSetErrorTest() throws JsonProcessingException {
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        final RequestDto requestDto = requestService.createAndSendRequest(controlDto);

        Mockito.verify(requestRepository, Mockito.times(2)).save(any());
        assertNotNull(requestDto);
        assertEquals(RequestStatusEnum.SEND_ERROR.name(), requestDto.getStatus());
        assertNotNull(requestDto.getError());
    }

    @Test
    void shouldUpdateResponse() {
        final String messageId = "messageId";
        final String eftiData = "<data>vive les datas</data>";
        final NotificationDto<?> notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(RetrieveMessageDto.builder()
                        .messageId(messageId)
                        .messageBodyDto(MessageBodyDto.builder().eFTIData(eftiData).build())
                        .build())
                .build();
        final ArgumentCaptor<RequestEntity> argumentCaptor = ArgumentCaptor.forClass(RequestEntity.class);
        when(requestRepository.findByControlRequestUuid(any())).thenReturn(requestEntity);
        when(requestRepository.save(any())).thenReturn(requestEntity);

        requestService.updateWithResponse(notificationDto);

        verify(requestRepository).save(argumentCaptor.capture());
        assertNotNull(argumentCaptor.getValue());
        assertEquals(RequestStatusEnum.RECEIVED.name(), argumentCaptor.getValue().getStatus());
        assertEquals(eftiData, new String(argumentCaptor.getValue().getControl().getEftiData()));
        assertEquals(StatusEnum.COMPLETE.name(), argumentCaptor.getValue().getControl().getStatus());
    }

    @Test
    void shouldReThrowException() {
        final String messageId = "messageId";
        final String eftiData = "<data>vive les datas</data>";

        final NotificationDto<?> notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(RetrieveMessageDto.builder()
                        .messageId(messageId)
                        .messageBodyDto(MessageBodyDto.builder().eFTIData(eftiData).build())
                        .build())
                .build();
        final ArgumentCaptor<RequestEntity> argumentCaptor = ArgumentCaptor.forClass(RequestEntity.class);
        when(requestRepository.findByControlRequestUuid(any())).thenReturn(null);

        assertThrows(RequestNotFoundException.class, () -> requestService.updateWithResponse(notificationDto));

        verify(requestRepository, never()).save(argumentCaptor.capture());
    }
}
