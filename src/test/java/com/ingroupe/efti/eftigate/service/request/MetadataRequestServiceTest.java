package com.ingroupe.efti.eftigate.service.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.TransportVehicleDto;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
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

import javax.mail.util.ByteArrayDataSource;
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
                        .vehicleId("abc123").countryStart("FR").countryEnd("toto").build(), TransportVehicleDto.builder()
                        .vehicleId("abc124").countryStart("BE").countryEnd("IT").build())).build();

        metadataResult = MetadataResult.builder()
                .eFTIDataUuid(DATA_UUID)
                .eFTIPlatformUrl(PLATFORM_URL)
                .transportVehicles(List.of(TransportVehicle.builder()
                        .vehicleId("abc123").vehicleCountry(CountryIndicator.FR).build(), TransportVehicle.builder()
                        .vehicleId("abc124").vehicleCountry(CountryIndicator.BE).build())).build();
        metadataRequestService = new MetadataRequestService(requestRepository, mapperUtils, rabbitSenderService, controlService, gateProperties, notificationService, metadataService);
    }

    @Test
    void shouldCreateAndSendRequest() {
        //Arrange
        when(requestRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());
        when(mapperUtils.requestToRequestDto(any())).thenReturn(requestDto);

        //Act
        metadataRequestService.createAndSendRequest(controlDto, "https://efti.platform.borduria.eu");

        //Assert
        verify(mapperUtils).requestDtoToRequestEntity(requestDtoArgumentCaptor.capture());
        assertEquals("https://efti.platform.borduria.eu", requestDtoArgumentCaptor.getValue().getGateUrlDest());
    }

    @Test
    void shouldCreateRequest() {
        //Arrange
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(mapperUtils.requestDtoToRequestEntity(any())).thenReturn(requestEntity);
        when(mapperUtils.requestToRequestDto(any())).thenReturn(requestDto);
        when(mapperUtils.metadataDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataRequestService.createRequest(controlDto, SUCCESS.name(), Collections.singletonList(metadataDto));

        //Assert
        verify(mapperUtils).requestDtoToRequestEntity(requestDtoArgumentCaptor.capture());
        assertEquals(List.of(metadataResult), requestDtoArgumentCaptor.getValue().getMetadataResults().getMetadataResult());
        assertEquals("SUCCESS", requestDtoArgumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldCreateRequestWithErrorStatus_WhenNoMetadata() {
        //Arrange
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(mapperUtils.requestDtoToRequestEntity(any())).thenReturn(requestEntity);
        when(mapperUtils.requestToRequestDto(any())).thenReturn(requestDto);

        //Act
        metadataRequestService.createRequest(controlDto, Collections.emptyList());

        //Assert
        verify(mapperUtils).requestDtoToRequestEntity(requestDtoArgumentCaptor.capture());
        assertEquals("ERROR", requestDtoArgumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldCreateRequestWithSuccessStatus_WhenMetadataNotEmpty() {
        //Arrange
        when(requestRepository.save(any())).thenReturn(requestEntity);
        when(mapperUtils.requestDtoToRequestEntity(any())).thenReturn(requestEntity);
        when(mapperUtils.requestToRequestDto(any())).thenReturn(requestDto);

        //Act
        metadataRequestService.createRequest(controlDto, Collections.singletonList(metadataDto));

        //Assert
        verify(mapperUtils).requestDtoToRequestEntity(requestDtoArgumentCaptor.capture());
        assertEquals("SUCCESS", requestDtoArgumentCaptor.getValue().getStatus());
    }
    @Test
    void trySendDomibusSuccessTest() throws SendRequestException, JsonProcessingException {
        metadataRequestService.sendRequest(requestDto);
        verify(rabbitSenderService).sendMessageToRabbit(any(), any(), any());
    }

    @Test
    void shouldManageMessageReceiveAndCreateNewControl_whenControlDoesNotExist() throws IOException {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(new ByteArrayDataSource(testFile("/json/FTI019.json"), "application/json"))
                        .build())
                .build();
        when(controlService.getControlByRequestUuid(anyString())).thenReturn(controlDto);
        when(controlService.createControlFrom(any(), any())).thenReturn(controlDto);
        when(mapperUtils.requestToRequestDto(any())).thenReturn(requestDto);

        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService).getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", "IN_PROGRESS");
        verify(controlService).createControlFrom(any(), any());
        verify(requestRepository, times(1)).save(any());
        verify(metadataService).search(any());
        verify(rabbitSenderService).sendMessageToRabbit(any(), any(), any());
    }





    @Test
    void shouldManageMessageReceiveAndUpdateExistingControl() throws IOException {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(new ByteArrayDataSource(testFile("/json/FTI020.json"), "application/json"))
                        .fromPartyId("gate")
                        .build())
                .build();
        controlEntity.setRequestType(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH.name());
        requestEntity.setStatus("IN_PROGRESS");
        controlEntity.setRequests(List.of(requestEntity));
        when(controlService.getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", "IN_PROGRESS")).thenReturn(controlEntity);
        when(mapperUtils.metadataResultDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService, never()).createControlFrom(any(), any());
        verify(metadataService, never()).search(any());
        verify(rabbitSenderService, never()).sendMessageToRabbit(any(), any(), any());

        verify(controlService).save(controlEntityArgumentCaptor.capture());
        assertEquals("COMPLETE", controlEntityArgumentCaptor.getValue().getStatus());
        assertFalse(controlEntityArgumentCaptor.getValue().getMetadataResults().getMetadataResult().isEmpty());
        assertFalse(controlEntityArgumentCaptor.getValue().getRequests().iterator().next().getMetadataResults().getMetadataResult().isEmpty());
    }

    @Test
    void shouldManageMessageReceiveAndUpdateExistingControlMetadatas() throws IOException {
        final NotificationDto notificationDto = NotificationDto.builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto.builder()
                        .messageId("messageId")
                        .body(new ByteArrayDataSource(testFile("/json/FTI020.json"), "application/json"))
                        .build())
                .build();
        controlEntity.setRequestType(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH.name());
        requestEntity.setStatus("IN_PROGRESS");
        requestEntity.setMetadataResults(metadataResults);
        controlEntity.setRequests(List.of(requestEntity));
        controlEntity.setMetadataResults(metadataResults);

        when(controlService.getControlForCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", "IN_PROGRESS")).thenReturn(controlEntity);
        when(mapperUtils.metadataResultDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataRequestService.manageMessageReceive(notificationDto);

        //assert
        verify(controlService).save(controlEntityArgumentCaptor.capture());
        assertEquals(2, controlEntityArgumentCaptor.getValue().getMetadataResults().getMetadataResult().size());
        assertEquals(1, controlEntityArgumentCaptor.getValue().getRequests().iterator().next().getMetadataResults().getMetadataResult().size());
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
        assertFalse(metadataRequestService.allRequestsContainsData(List.of(requestEntity)));
    }

    @Test
    void allRequestsContainsDataTest_whenTrue() {
        //Arrange
        requestEntity.setMetadataResults(new MetadataResults());
        //Act and Assert
        assertTrue(metadataRequestService.allRequestsContainsData(List.of(requestEntity)));
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
