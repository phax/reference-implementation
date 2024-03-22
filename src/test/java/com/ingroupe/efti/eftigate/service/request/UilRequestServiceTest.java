package com.ingroupe.efti.eftigate.service.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
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
import com.ingroupe.efti.eftigate.service.ControlService;
import com.ingroupe.efti.eftigate.service.RabbitSenderService;
import com.ingroupe.efti.eftigate.service.RequestServiceTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UilRequestServiceTest extends RequestServiceTest {

    AutoCloseable openMocks;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestSendingService requestSendingService;
    @Mock
    private ControlService controlService;
    @Mock
    private RabbitSenderService rabbitSenderService;
    private UilRequestService uilRequestService;
    private final UilDto uilDto = new UilDto();
    private final ControlDto controlDto = new ControlDto();
    private final ControlEntity controlEntity = new ControlEntity();
    private final RequestEntity requestEntity = new RequestEntity();
    private final RequestDto requestDto = new RequestDto();

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);

        GateProperties gateProperties = GateProperties.builder().ap(GateProperties.ApConfig.builder().url("url").password("pwd").username("usr").build()).build();
        uilRequestService = new UilRequestService(requestRepository, mapperUtils, rabbitSenderService, controlService);

        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        String requestUuid = UUID.randomUUID().toString();

        this.uilDto.setEFTIGateUrl("gate");
        this.uilDto.setEFTIDataUuid("uuid");
        this.uilDto.setEFTIPlatformUrl("plateform");
        this.controlDto.setEftiDataUuid(uilDto.getEFTIDataUuid());
        this.controlDto.setEftiGateUrl(uilDto.getEFTIGateUrl());
        this.controlDto.setEftiPlatformUrl(uilDto.getEFTIPlatformUrl());
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
    void trySendDomibusSucessTest() throws SendRequestException, JsonProcessingException {
        when(requestSendingService.sendRequest(any(), any())).thenReturn("result");
        when(requestRepository.save(any())).thenReturn(requestEntity);

        uilRequestService.sendRequest(requestDto);
        verify(rabbitSenderService).sendMessageToRabbit(any(), any(), any());
    }

    @Test
    void sendTest() throws SendRequestException, InterruptedException {
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(requestSendingService.sendRequest(any(),any())).thenThrow(SendRequestException.class);

        uilRequestService.createAndSendRequest(controlDto, null);

        Thread.sleep(1000);
        verify(requestRepository, Mockito.times(1)).save(any());
    }

    @Test
    void shouldUpdateResponse() throws IOException {
        final String messageId = "messageId";
        final String eftiData = """
                  {
                    "requestUuid": "test",
                    "status": "COMPLETE",
                    "eFTIData": "<data>vive les datas<data>"
                  }""";
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId(messageId)
                        .body(new ByteArrayDataSource(eftiData, "osef"))
                        .build())
                .build();
        final ArgumentCaptor<RequestEntity> argumentCaptor = ArgumentCaptor.forClass(RequestEntity.class);
        when(requestRepository.findByControlRequestUuid(any())).thenReturn(requestEntity);
        when(requestRepository.save(any())).thenReturn(requestEntity);
        uilRequestService.updateWithResponse(notificationDto);

        //verify(controlService).setEftiData(controlDto, "<data>vive les datas<data>".getBytes(StandardCharsets.UTF_8));
        verify(requestRepository).save(argumentCaptor.capture());
        assertNotNull(argumentCaptor.getValue());
        assertEquals(RequestStatusEnum.RECEIVED.name(), argumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldUpdateErrorResponse() throws IOException {
        final String messageId = "messageId";
        final String eftiData = """
                  {
                    "requestUuid": "test",
                    "status": "ERROR",
                    "eFTIData": "<data>vive les datas<data>"
                  }""";
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId(messageId)
                        .body(new ByteArrayDataSource(eftiData, "osef"))
                        .build())
                .build();
        final ArgumentCaptor<RequestEntity> argumentCaptor = ArgumentCaptor.forClass(RequestEntity.class);
        when(requestRepository.findByControlRequestUuid(any())).thenReturn(requestEntity);
        when(requestRepository.save(any())).thenReturn(requestEntity);
        uilRequestService.updateWithResponse(notificationDto);

        verify(requestRepository, times(2)).save(argumentCaptor.capture());
        assertNotNull(argumentCaptor.getValue());
        assertEquals(RequestStatusEnum.RECEIVED.name(), argumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldUpdateResponseSendFailure() {
        final String messageId = "messageId";
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.SEND_FAILURE)
                .content(NotificationContentDto.builder()
                        .messageId(messageId)
                        .build())
                .build();
        final ArgumentCaptor<RequestEntity> argumentCaptor = ArgumentCaptor.forClass(RequestEntity.class);
        when(requestRepository.findByEdeliveryMessageId(any())).thenReturn(requestEntity);
        when(requestRepository.save(any())).thenReturn(requestEntity);
        uilRequestService.updateWithResponse(notificationDto);
        verify(requestRepository).save(argumentCaptor.capture());
        assertNotNull(argumentCaptor.getValue());
        assertEquals(RequestStatusEnum.SEND_ERROR.name(), argumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldThrowIfMessageNotFound() {
        final String messageId = "messageId";
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.SEND_FAILURE)
                .content(NotificationContentDto.builder()
                        .messageId(messageId)
                        .build())
                .build();
        when(requestRepository.findByEdeliveryMessageId(any())).thenReturn(null);
        assertThrows(RequestNotFoundException.class, () -> uilRequestService.updateWithResponse(notificationDto));
    }

    @Test
    void shouldReThrowException() throws IOException {
        final String messageId = "messageId";
        final String eftiData = """
                {
                  "requestUuid": "test",
                  "status": "toto"
                }""";

        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId(messageId)
                        .body(new ByteArrayDataSource(eftiData, "osef"))
                        .build())
                .build();
        final ArgumentCaptor<RequestEntity> argumentCaptor = ArgumentCaptor.forClass(RequestEntity.class);
        when(requestRepository.findByControlRequestUuid(any())).thenReturn(null);

        assertThrows(RequestNotFoundException.class, () -> uilRequestService.updateWithResponse(notificationDto));

        verify(requestRepository, never()).save(argumentCaptor.capture());
    }

    @Test
    void allRequestsContainsDataTest_whenFalse() {
        //Arrange
        when(getRequestRepository().save(any())).thenReturn(getRequestEntity());
        //Act and Assert
        assertFalse(uilRequestService.allRequestsContainsData(List.of(getRequestEntity())));
    }

    @Test
    void allRequestsContainsDataTest_whenTrue() {
        //Arrange
        byte[] data = {10, 20, 30, 40};
        requestEntity.setReponseData(data);
        getRequestEntity().setReponseData(data);
        //Act and Assert
        assertTrue(uilRequestService.allRequestsContainsData(List.of(getRequestEntity())));
    }

    @Test
    void getDataFromRequestsTest() {
        //Arrange
        byte[] data1 = {10, 20, 30, 40};
        byte[] data2 = {60, 80, 70, 10};

        getRequestEntity().setReponseData(data1);
        getSecondRequestEntity().setReponseData(data2);
        final ControlEntity controlEntity = ControlEntity.builder().requests(List.of(getRequestEntity(), getSecondRequestEntity())).build();
        //Act
        uilRequestService.setDataFromRequests(controlEntity);

        //Assert
        assertNotNull(controlEntity.getEftiData());
        assertEquals(8, controlEntity.getEftiData().length);
    }
}
