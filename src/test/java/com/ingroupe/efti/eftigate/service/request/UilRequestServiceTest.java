package com.ingroupe.efti.eftigate.service.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.service.BaseServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.List;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UilRequestServiceTest extends BaseServiceTest {
    private UilRequestService uilRequestService;

    @Captor
    ArgumentCaptor<RequestEntity> requestEntityArgumentCaptor;

    @Override
    @BeforeEach
    public void before() {
        super.before();
        uilRequestService = new UilRequestService(requestRepository, mapperUtils, rabbitSenderService, controlService, gateProperties, notificationService, serializeUtils);
    }

    @Test
    void receiveGateRequestFromOtherGateSucessTest() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        final String messageId = "e94806cd-e52b-11ee-b7d3-0242ac120012@domibus.eu";
        final String content = "{\"requestUuid\":\"24414689-1abf-4a9f-b4df-de3a491a44c9\",\"subsetEU\":[],\"subsetMS\":[],\"authority\":null,\"eFTIPlatformUrl\":\"http://efti.platform.acme.com\",\"eFTIDataUuid\":\"12345678-ab12-4ab6-8999-123456789abc\", \"eFTIData\":\"oki\"}";
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .action("forwardUil")
                        .body(new ByteArrayDataSource(content, "application/json"))
                        .contentType("application/json")
                        .fromPartyId("http://efti.gate.listenbourg.eu")
                        .messageId(messageId)
                        .build())
                .build();

        Mockito.when(requestRepository.findByControlRequestUuidAndStatus(any(), any())).thenReturn(requestEntity);
        Mockito.when(requestRepository.save(any())).thenReturn(requestEntity);

        uilRequestService.receiveGateRequest(notificationDto);

        verify(requestRepository).save(requestEntityArgumentCaptor.capture());
        assertEquals(RequestStatusEnum.SUCCESS, requestEntityArgumentCaptor.getValue().getStatus());
    }

    @Test
    void receiveGateRequestFromOtherGateErrorTest() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        final String messageId = "e94806cd-e52b-11ee-b7d3-0242ac120012@domibus.eu";
        final String content = "{\"requestUuid\":\"24414689-1abf-4a9f-b4df-de3a491a44c9\",\"subsetEU\":[],\"subsetMS\":[],\"authority\":null,\"eFTIPlatformUrl\":\"http://efti.platform.acme.com\",\"eFTIDataUuid\":\"12345678-ab12-4ab6-8999-123456789abc\"}";
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .action("forwardUil")
                        .body(new ByteArrayDataSource(content, "application/json"))
                        .contentType("application/json")
                        .fromPartyId("http://efti.gate.listenbourg.eu")
                        .messageId(messageId)
                        .build())
                .build();
        final ArgumentCaptor<RequestEntity> argumentCaptorRequestEntity = ArgumentCaptor.forClass(RequestEntity.class);

        Mockito.when(requestRepository.findByControlRequestUuidAndStatus(any(), any())).thenReturn(requestEntity);
        Mockito.when(requestRepository.findByControlRequestUuidAndStatus(any(), any())).thenReturn(requestEntity);

        uilRequestService.receiveGateRequest(notificationDto);

        verify(requestRepository).save(argumentCaptorRequestEntity.capture());
        assertEquals(RequestStatusEnum.ERROR, argumentCaptorRequestEntity.getValue().getStatus());
    }

    @Test
    void receiveGateRequestSucessTest() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        final String messageId = "e94806cd-e52b-11ee-b7d3-0242ac120012@domibus.eu";
        final String content = "{\"requestUuid\":\"24414689-1abf-4a9f-b4df-de3a491a44c9\",\"subsetEU\":[],\"subsetMS\":[],\"authority\":null,\"eFTIPlatformUrl\":\"http://efti.platform.acme.com\",\"eFTIDataUuid\":\"12345678-ab12-4ab6-8999-123456789abc\"}";
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .action("forwardUil")
                        .body(new ByteArrayDataSource(content, "osef"))
                        .contentType("application/json")
                        .fromPartyId("http://efti.gate.listenbourg.eu")
                        .messageId(messageId)
                        .build())
                .build();
        final ControlDto controlDto = ControlDto.fromGateToGateMessageBodyDto(mapper.readValue(content, MessageBodyDto.class), RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH, notificationDto, "http://france.fr");
        final ArgumentCaptor<ControlDto> argumentCaptorControlDto = ArgumentCaptor.forClass(ControlDto.class);
        final ArgumentCaptor<RequestEntity> argumentCaptorRequestEntity = ArgumentCaptor.forClass(RequestEntity.class);

        Mockito.when(controlService.save(any(ControlDto.class))).thenReturn(controlDto);
        when(requestRepository.save(any())).thenReturn(requestEntity);

        uilRequestService.receiveGateRequest(notificationDto);

        verify(controlService).save(argumentCaptorControlDto.capture());
        assertEquals(RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH, argumentCaptorControlDto.getValue().getRequestType());
    }

    @Test
    void trySendDomibusSuccessTest() throws SendRequestException, JsonProcessingException {
        uilRequestService.sendRequest(requestDto);
        verify(rabbitSenderService).sendMessageToRabbit(any(), any(), any());
    }

    @Test
    void sendTest() throws JsonProcessingException {
        when(requestRepository.save(any())).thenReturn(requestEntity);

        uilRequestService.createAndSendRequest(controlDto, null);

        verify(requestRepository, Mockito.times(1)).save(any());
        verify(rabbitSenderService, Mockito.times(1)).sendMessageToRabbit(any(), any(), any());

    }

    @Test
    void shouldUpdateResponseSucessFromPlatformAndShoulSendToGate() throws IOException {
        final String messageId = "e94806cd-e52b-11ee-b7d3-0242ac120012@domibus.eu";
        final String eftiData = """
                  {
                    "requestUuid": "test",
                    "status": "COMPLETE",
                    "eFTIData": "<data>vive les datas<data>"
                  }""";
        this.requestEntity.getControl().setRequestType(RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH);
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId(messageId)
                        .body(new ByteArrayDataSource(eftiData, "osef"))
                        .build())
                .build();
        final ArgumentCaptor<ControlDto> argumentCaptorControlDto = ArgumentCaptor.forClass(ControlDto.class);
        requestEntity.getControl().setFromGateUrl("other");
        when(requestRepository.findByControlRequestUuidAndStatus(any(), any())).thenReturn(requestEntity);
        when(requestRepository.save(any())).thenReturn(requestEntity);

        uilRequestService.updateWithResponse(notificationDto);

        verify(controlService).save(argumentCaptorControlDto.capture());
        assertEquals(RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH, argumentCaptorControlDto.getValue().getRequestType());
    }

    @Test
    void shouldUpdateResponse() throws IOException {
        final String messageId = "e94806cd-e52b-11ee-b7d3-0242ac120012@domibus.eu";
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
        when(requestRepository.findByControlRequestUuidAndStatus(any(), any())).thenReturn(requestEntity);
        when(requestRepository.save(any())).thenReturn(requestEntity);
        uilRequestService.updateWithResponse(notificationDto);

        verify(requestRepository).save(argumentCaptor.capture());
        assertNotNull(argumentCaptor.getValue());
        assertEquals(RequestStatusEnum.SUCCESS, argumentCaptor.getValue().getStatus());
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
        when(requestRepository.findByControlRequestUuidAndStatus(any(), any())).thenReturn(requestEntity);
        when(requestRepository.save(any())).thenReturn(requestEntity);
        uilRequestService.updateWithResponse(notificationDto);

        verify(requestRepository, times(2)).save(argumentCaptor.capture());
        assertNotNull(argumentCaptor.getValue());
        assertEquals(RequestStatusEnum.ERROR, argumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldUpdateStatus() {
        final ArgumentCaptor<RequestEntity> requestEntityArgumentCaptor = ArgumentCaptor.forClass(RequestEntity.class);
        when(requestRepository.save(any())).thenReturn(requestEntity);
        uilRequestService.updateStatus(requestDto, ERROR, new NotificationDto());
        verify(requestRepository).save(requestEntityArgumentCaptor.capture());
        verify(requestRepository,  Mockito.times(1)).save(any(RequestEntity.class));
        assertEquals(ERROR, requestEntityArgumentCaptor.getValue().getStatus());
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
        assertEquals(RequestStatusEnum.SEND_ERROR, argumentCaptor.getValue().getStatus());
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
        when(requestRepository.findByControlRequestUuidAndStatus(any(), any())).thenReturn(null);

        assertThrows(RequestNotFoundException.class, () -> uilRequestService.updateWithResponse(notificationDto));

        verify(requestRepository, never()).save(argumentCaptor.capture());
    }

    @Test
    void allRequestsContainsDataTest_whenFalse() {
        //Act and Assert
        assertFalse(uilRequestService.allRequestsContainsData(List.of(requestEntity)));
    }

    @Test
    void allRequestsContainsDataTest_whenTrue() {
        //Arrange
        final byte[] data = {10, 20, 30, 40};
        requestEntity.setReponseData(data);
        requestEntity.setReponseData(data);
        //Act and Assert
        assertTrue(uilRequestService.allRequestsContainsData(List.of(requestEntity)));
    }

    @Test
    void getDataFromRequestsTest() {
        //Arrange
        final byte[] data1 = {10, 20, 30, 40};
        final byte[] data2 = {60, 80, 70, 10};

        requestEntity.setReponseData(data1);
        secondRequestEntity.setReponseData(data2);
        final ControlEntity controlEntity = ControlEntity.builder().requests(List.of(requestEntity, secondRequestEntity)).build();
        //Act
        uilRequestService.setDataFromRequests(controlEntity);

        //Assert
        assertNotNull(controlEntity.getEftiData());
        assertEquals(8, controlEntity.getEftiData().length);
    }
}
