package com.ingroupe.efti.eftigate.service.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.TransportVehicleDto;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.service.BaseServiceTest;
import com.ingroupe.efti.metadataregistry.entity.TransportVehicle;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.SUCCESS;
import static com.ingroupe.efti.eftigate.EftiTestUtils.testFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataRequestServiceTest extends BaseServiceTest {
    @Mock
    private MetadataService metadataService;
    private MetadataRequestService metadataRequestService;
    @Captor
    ArgumentCaptor<RequestDto> requestDtoArgumentCaptor;

    @Captor
    ArgumentCaptor<ControlEntity> controlEntityArgumentCaptor;

    public static final String DATA_UUID = "12345678-ab12-4ab6-8999-123456789abc";
    public static final String PLATFORM_URL = "http://efti.platform.truc.eu";
    private MetadataDto metadataDto;
    private MetadataResult metadataResult;

    @Override
    @BeforeEach
    public void before() {
        super.before();
        metadataDto = MetadataDto.builder()
                .eFTIDataUuid(DATA_UUID)
                .eFTIPlatformUrl(PLATFORM_URL)
                .transportVehicles(List.of(TransportVehicleDto.builder()
                        .vehicleId("abc123").countryStart("FR").vehicleCountry("FR").countryEnd("toto").build(), TransportVehicleDto.builder()
                        .vehicleId("abc124").countryStart("BE").vehicleCountry("BE").countryEnd("IT").build())).build();

        metadataResult = MetadataResult.builder()
                .eFTIDataUuid(DATA_UUID)
                .eFTIPlatformUrl(PLATFORM_URL)
                .transportVehicles(List.of(TransportVehicle.builder()
                        .vehicleId("abc123").vehicleCountry(CountryIndicator.FR).build(), TransportVehicle.builder()
                        .vehicleId("abc124").vehicleCountry(CountryIndicator.BE).build())).build();
        metadataRequestService = new MetadataRequestService(requestRepository, mapperUtils, rabbitSenderService, controlService, gateProperties, metadataService, requestUpdaterService, serializeUtils);
    }

    @Test
    void shouldCreateAndSendRequest() {
        //Arrange
        when(requestRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

        //Act
        metadataRequestService.createAndSendRequest(controlDto, "https://efti.platform.borduria.eu");

        //Assert
        verify(mapperUtils, times(2)).requestDtoToRequestEntity(requestDtoArgumentCaptor.capture());
        assertEquals("https://efti.platform.borduria.eu", requestDtoArgumentCaptor.getValue().getGateUrlDest());
    }

    @Test
    void shouldCreateRequest() {
        //Arrange
        when(requestRepository.save(any())).thenReturn(requestEntity);

        //Act
        metadataRequestService.createRequest(controlDto, SUCCESS, Collections.singletonList(metadataDto));

        //Assert
        verify(mapperUtils).requestDtoToRequestEntity(requestDtoArgumentCaptor.capture());
        assertEquals(metadataResult.getMetadataUUID(), requestDtoArgumentCaptor.getValue().getMetadataResults().getMetadataResult().get(0).getMetadataUUID());
        assertEquals(metadataResult.getEFTIDataUuid(), requestDtoArgumentCaptor.getValue().getMetadataResults().getMetadataResult().get(0).getEFTIDataUuid());
        assertEquals(metadataResult.getEFTIPlatformUrl(), requestDtoArgumentCaptor.getValue().getMetadataResults().getMetadataResult().get(0).getEFTIPlatformUrl());
        assertEquals(metadataResult.getTransportVehicles().size(), requestDtoArgumentCaptor.getValue().getMetadataResults().getMetadataResult().get(0).getTransportVehicles().size());
        assertEquals(SUCCESS, requestDtoArgumentCaptor.getValue().getStatus());
    }
    
    @Test
    void trySendDomibusSuccessTest() throws SendRequestException, JsonProcessingException {
        when(requestRepository.save(any())).thenReturn(requestEntity);

        metadataRequestService.sendRequest(requestDto);
        verify(rabbitSenderService).sendMessageToRabbit(any(), any(), any());
    }

    @Test
    void shouldManageMessageReceiveAndCreateNewControl_whenControlDoesNotExist() throws IOException {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(testFile("/json/FTI019.xml"))
                        .build())
                .build();
        when(controlService.getControlByRequestUuid(anyString())).thenReturn(controlDto);
        when(controlService.createControlFrom(any(), any(), any())).thenReturn(controlDto);
        when(requestRepository.save(any())).thenReturn(requestEntity);
        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService).getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", RequestStatusEnum.IN_PROGRESS);
        verify(controlService).createControlFrom(any(), any(), any());
        verify(requestRepository, times(2)).save(any());
        verify(metadataService).search(any());
        verify(rabbitSenderService).sendMessageToRabbit(any(), any(), any());
    }

    @Test
    void shouldManageMessageReceiveAndUpdateExistingControlStatusAsComplete() throws IOException {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(testFile("/json/FTI020.xml"))
                        .fromPartyId("gate")
                        .build())
                .build();
        controlEntity.setRequestType(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH);
        requestEntity.setStatus(RequestStatusEnum.IN_PROGRESS);
        controlEntity.setRequests(List.of(requestEntity));
        when(controlService.getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", RequestStatusEnum.IN_PROGRESS)).thenReturn(controlEntity);
        when(mapperUtils.metadataResultDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService, never()).createControlFrom(any(), any(), any());
        verify(metadataService, never()).search(any());
        verify(rabbitSenderService, never()).sendMessageToRabbit(any(), any(), any());

        verify(controlService).save(controlEntityArgumentCaptor.capture());
        assertEquals(StatusEnum.COMPLETE, controlEntityArgumentCaptor.getValue().getStatus());
        assertEquals(SUCCESS, controlEntityArgumentCaptor.getValue().getRequests().iterator().next().getStatus());
        assertFalse(controlEntityArgumentCaptor.getValue().getMetadataResults().getMetadataResult().isEmpty());
        assertFalse(controlEntityArgumentCaptor.getValue().getRequests().iterator().next().getMetadataResults().getMetadataResult().isEmpty());
    }

    @Test
    void shouldManageMessageReceiveAndUpdateExistingControlStatusAsError_whenSomeRequestsAreInErrorStatus() throws IOException {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(testFile("/json/FTI020.xml"))
                        .fromPartyId("gate")
                        .build())
                .build();
        controlEntity.setRequestType(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH);
        secondRequestEntity.setStatus(RequestStatusEnum.ERROR);
        requestEntity.setStatus(RequestStatusEnum.IN_PROGRESS);
        controlEntity.setRequests(List.of(requestEntity, secondRequestEntity));
        when(controlService.getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", RequestStatusEnum.IN_PROGRESS)).thenReturn(controlEntity);
        when(mapperUtils.metadataResultDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService, never()).createControlFrom(any(), any(), any());
        verify(metadataService, never()).search(any());
        verify(rabbitSenderService, never()).sendMessageToRabbit(any(), any(), any());

        verify(controlService).save(controlEntityArgumentCaptor.capture());
        assertEquals(StatusEnum.ERROR, controlEntityArgumentCaptor.getValue().getStatus());
        assertFalse(controlEntityArgumentCaptor.getValue().getMetadataResults().getMetadataResult().isEmpty());
        assertFalse(controlEntityArgumentCaptor.getValue().getRequests().iterator().next().getMetadataResults().getMetadataResult().isEmpty());
    }

    @Test
    void shouldManageMessageReceiveAndUpdateExistingControlStatusAsTimeout_whenSomeRequestsAreInTimeoutStatus() throws IOException {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(testFile("/json/FTI020.xml"))
                        .fromPartyId("gate")
                        .build())
                .build();
        controlEntity.setRequestType(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH);
        secondRequestEntity.setStatus(RequestStatusEnum.TIMEOUT);
        requestEntity.setStatus(RequestStatusEnum.IN_PROGRESS);
        controlEntity.setRequests(List.of(requestEntity, secondRequestEntity));
        when(controlService.getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", RequestStatusEnum.IN_PROGRESS)).thenReturn(controlEntity);
        when(mapperUtils.metadataResultDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService, never()).createControlFrom(any(), any(), any());
        verify(metadataService, never()).search(any());
        verify(rabbitSenderService, never()).sendMessageToRabbit(any(), any(), any());

        verify(controlService).save(controlEntityArgumentCaptor.capture());
        assertEquals(StatusEnum.TIMEOUT, controlEntityArgumentCaptor.getValue().getStatus());
        assertFalse(controlEntityArgumentCaptor.getValue().getMetadataResults().getMetadataResult().isEmpty());
        assertFalse(controlEntityArgumentCaptor.getValue().getRequests().iterator().next().getMetadataResults().getMetadataResult().isEmpty());
    }

    @Test
    void shouldManageMessageReceiveAndUpdateExistingControlStatusAseRROR_whenOneRequestsIsInErrorStatus() throws IOException {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(testFile("/json/FTI020.xml"))
                        .fromPartyId("gate")
                        .build())
                .build();
        controlEntity.setRequestType(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH);
        secondRequestEntity.setStatus(RequestStatusEnum.TIMEOUT);
        requestEntity.setStatus(RequestStatusEnum.ERROR);
        controlEntity.setRequests(List.of(requestEntity, secondRequestEntity));
        when(controlService.getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", RequestStatusEnum.IN_PROGRESS)).thenReturn(controlEntity);
        when(mapperUtils.metadataResultDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService, never()).createControlFrom(any(), any(), any());
        verify(metadataService, never()).search(any());
        verify(rabbitSenderService, never()).sendMessageToRabbit(any(), any(), any());

        verify(controlService).save(controlEntityArgumentCaptor.capture());
        assertEquals(StatusEnum.ERROR, controlEntityArgumentCaptor.getValue().getStatus());
        assertFalse(controlEntityArgumentCaptor.getValue().getMetadataResults().getMetadataResult().isEmpty());
    }

    @Test
    void shouldManageMessageReceiveAndUpdateExistingControlMetadatas() {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(testFile("/json/FTI020.xml"))
                        .build())
                .build();
        controlEntity.setRequestType(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH);
        requestEntity.setStatus(RequestStatusEnum.IN_PROGRESS);
        requestEntity.setMetadataResults(metadataResults);
        controlEntity.setRequests(List.of(requestEntity));
        controlEntity.setMetadataResults(metadataResults);

        when(controlService.getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", RequestStatusEnum.IN_PROGRESS)).thenReturn(controlEntity);
        when(mapperUtils.metadataResultDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService).save(controlEntityArgumentCaptor.capture());
        assertEquals(2, controlEntityArgumentCaptor.getValue().getMetadataResults().getMetadataResult().size());
        assertEquals(1, controlEntityArgumentCaptor.getValue().getRequests().iterator().next().getMetadataResults().getMetadataResult().size());
    }

    @Test
    void allRequestsContainsDataTest_whenFalse() {
        assertFalse(metadataRequestService.allRequestsContainsData(List.of(requestEntity)));
    }

    @Test
    void allRequestsContainsDataTest_whenTrue() {
        //Arrange
        final MetadataResult metadataResult = new MetadataResult();
        metadataResult.setCountryStart("FR");
        metadataResult.setCountryEnd("FR");
        metadataResult.setDisabled(false);
        metadataResult.setDangerousGoods(true);
        requestEntity.setMetadataResults(new MetadataResults(List.of(metadataResult)));
        //Act and Assert
        assertTrue(metadataRequestService.allRequestsContainsData(List.of(requestEntity)));
    }

    @Test
    void getDataFromRequestsTest() {
        //Arrange
        final MetadataResult metadataResult1 = new MetadataResult();
        metadataResult1.setCountryStart("FR");
        metadataResult1.setCountryEnd("FR");
        metadataResult1.setDisabled(false);
        metadataResult1.setDangerousGoods(true);

        final MetadataResults metadataResults1 = new MetadataResults();
        metadataResults1.setMetadataResult(List.of(metadataResult1));

        final MetadataResult metadataResult2 = new MetadataResult();
        metadataResult2.setCountryStart("FR");
        metadataResult2.setCountryEnd("FR");
        metadataResult2.setDisabled(false);
        metadataResult2.setDangerousGoods(true);

        final MetadataResults metadataResults2 = new MetadataResults();
        metadataResults2.setMetadataResult(List.of(metadataResult2));

        requestEntity.setMetadataResults(metadataResults1);
        secondRequestEntity.setMetadataResults(metadataResults2);

        final ControlEntity controlEntity = ControlEntity.builder().requests(List.of(requestEntity, secondRequestEntity)).build();
        //Act
        metadataRequestService.setDataFromRequests(controlEntity);

        //Assert
        assertNotNull(controlEntity.getMetadataResults());
        assertEquals(2, controlEntity.getMetadataResults().getMetadataResult().size());
    }
}
