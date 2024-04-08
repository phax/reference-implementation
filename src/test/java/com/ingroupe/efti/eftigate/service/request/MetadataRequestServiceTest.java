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
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.ControlService;
import com.ingroupe.efti.eftigate.service.RabbitSenderService;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetadataRequestServiceTest extends RequestServiceTest {

    AutoCloseable openMocks;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestSendingService requestSendingService;
    @Mock
    private ControlService controlService;
    @Mock
    private RabbitSenderService rabbitSenderService;
    @Mock
    private MetadataService metadataService;
    @MockBean
    private MetadataRequestService metadataRequestService;
    @Mock
    private MetadataLocalRequestService metadataLocalRequestService;


    private final UilDto uilDto = new UilDto();
    private final ControlDto controlDto = new ControlDto();
    private final ControlEntity controlEntity = new ControlEntity();
    private final RequestEntity requestEntity = new RequestEntity();
    private final RequestDto requestDto = new RequestDto();

    @Override
    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);

        GateProperties gateProperties = GateProperties.builder().ap(GateProperties.ApConfig.builder().url("url").password("pwd").username("usr").build()).build();
        metadataRequestService = new MetadataRequestService(requestRepository, getMapperUtils(), rabbitSenderService, controlService, gateProperties, metadataService, metadataLocalRequestService);

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

    @Override
    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void shouldCreateRequest() throws JsonProcessingException {
        //Arrange
        final ArgumentCaptor<RequestDto> argumentCaptor = ArgumentCaptor.forClass(RequestDto.class);
        when(getRequestRepository().save(any())).then(AdditionalAnswers.returnsFirstArg());
        when(getMapperUtils().requestToRequestDto(any())).thenReturn(getRequestDto());

        //Act
        metadataRequestService.createAndSendRequest(controlDto, "https://efti.platform.borduria.eu");

        //Assert
        verify(getMapperUtils()).requestDtoToRequestEntity(argumentCaptor.capture());
        assertEquals("https://efti.platform.borduria.eu", argumentCaptor.getValue().getGateUrlDest());
    }

    @Test
    void trySendDomibusSuccessTest() throws SendRequestException, JsonProcessingException {
        when(requestSendingService.sendRequest(any(), any())).thenReturn("result");
        when(requestRepository.save(any())).thenReturn(requestEntity);

        metadataRequestService.sendRequest(requestDto);
        verify(rabbitSenderService).sendMessageToRabbit(any(), any(), any());
    }

    @Test
    void shouldManageMessageReceive() throws IOException {
        final String messageId = "messageId";
        final String eftiData = """
                  { 
                    "eFTIGate": "FR",
                    "requestUuid": "test",
                    "status": "COMPLETE",
                    "metadata": "<data>vive les datas<data>"
                  }""";
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId(messageId)
                        .body(new ByteArrayDataSource(eftiData, "osef"))
                        .build())
                .build();
        when(controlService.save(any())).thenReturn(getControlDto());
        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService).save(any());
        verify(metadataService).search(any());
        verify(metadataLocalRequestService).createRequest(any(ControlDto.class), anyString(), anyList());
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
        metadataRequestService.updateWithResponse(notificationDto);
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
        assertThrows(RequestNotFoundException.class, () -> metadataRequestService.updateWithResponse(notificationDto));
    }

    @Test
    void allRequestsContainsDataTest_whenFalse() {
        //Arrange
        when(getRequestRepository().save(any())).thenReturn(getRequestEntity());
        //Act and Assert
        assertFalse(metadataRequestService.allRequestsContainsData(List.of(getRequestEntity())));
    }

    @Test
    void allRequestsContainsDataTest_whenTrue() {
        //Arrange
        getRequestEntity().setMetadataResults(new MetadataResults());
        //Act and Assert
        assertTrue(metadataRequestService.allRequestsContainsData(List.of(getRequestEntity())));
    }

    @Test
    void getDataFromRequestsTest() {
        //Arrange
        MetadataResult metadataResult1 = new MetadataResult();
        metadataResult1.setCountryStart("FR");
        metadataResult1.setCountryEnd("FR");
        metadataResult1.setDisabled(false);
        metadataResult1.setDangerousGoods(true);

        MetadataResults metadataResults1 = new MetadataResults();
        metadataResults1.setMetadataResult(List.of(metadataResult1));

        MetadataResult metadataResult2 = new MetadataResult();
        metadataResult2.setCountryStart("FR");
        metadataResult2.setCountryEnd("FR");
        metadataResult2.setDisabled(false);
        metadataResult2.setDangerousGoods(true);

        MetadataResults metadataResults2 = new MetadataResults();
        metadataResults2.setMetadataResult(List.of(metadataResult2));

        getRequestEntity().setMetadataResults(metadataResults1);
        getSecondRequestEntity().setMetadataResults(metadataResults2);

        final ControlEntity controlEntity = ControlEntity.builder().requests(List.of(getRequestEntity(), getSecondRequestEntity())).build();
        //Act
        metadataRequestService.setDataFromRequests(controlEntity);

        //Assert
        assertNotNull(controlEntity.getMetadataResults());
        assertEquals(2, controlEntity.getMetadataResults().getMetadataResult().size());
    }
}
