package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.AuthorityDto;
import com.ingroupe.efti.commons.dto.ContactInformationDto;
import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.MetadataResponseDto;
import com.ingroupe.efti.commons.dto.TransportVehicleDto;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class ControlServiceTest extends AbstractServiceTest {

    AutoCloseable openMocks;
    @Mock
    private ControlRepository controlRepository;
    @Mock
    private UilSearchRequestService defaultUilSearchRequestService;

    @Mock
    private MetadataSearchRequestService defaultMetadataSearchRequestService;
    private ControlService controlService;
    @Mock
    private MetadataService metadataService;

    @Mock
    private EftiAsyncCallsProcessor eftiAsyncCallsProcessor;

    private final UilDto uilDto = new UilDto();
    private final MetadataRequestDto metadataRequestDto = new MetadataRequestDto();
    private final ControlDto controlDto = new ControlDto();
    MetadataDto metadataDto= new MetadataDto();
    TransportVehicleDto transportVehicleDto = new TransportVehicleDto();
    private final ControlEntity controlEntity = ControlEntity.builder().requestType(RequestTypeEnum.LOCAL_UIL_SEARCH.name()).build();
    private final RequestEntity requestEntity = new RequestEntity();

    MetadataResult metadataResult = new MetadataResult();
    MetadataResults metadataResults = new MetadataResults();
    private final RequestUuidDto requestUuidDto = new RequestUuidDto();
    private final String requestUuid = UUID.randomUUID().toString();
    private final String metadataUuid = UUID.randomUUID().toString();

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);
        controlService = new ControlService(controlRepository, mapperUtils, defaultUilSearchRequestService, List.of(defaultMetadataSearchRequestService, defaultUilSearchRequestService), eftiAsyncCallsProcessor);

        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        final String status = StatusEnum.PENDING.toString();
        final AuthorityDto authorityDto = AuthorityDto.builder()
                .nationalUniqueIdentifier("national identifier")
                .name("Robert")
                .workingContact(ContactInformationDto.builder()
                        .email("toto@gmail.com")
                        .city("Acheville")
                        .buildingNumber("12")
                        .postalCode("62320")
                        .streetName("rue jean luc de la rue").build())
                .country("FR")
                .legalContact(ContactInformationDto.builder()
                        .email("toto@gmail.com")
                        .city("Acheville")
                        .buildingNumber("12")
                        .postalCode("62320")
                        .streetName("rue jean luc de la rue").build())
                .isEmergencyService(true).build();

        requestUuidDto.setRequestUuid(requestUuid);
        requestUuidDto.setStatus(status);

        this.uilDto.setEFTIGateUrl("http://www.gate.com");
        this.uilDto.setEFTIDataUuid("12345678-ab12-4ab6-8999-123456789abc");
        this.uilDto.setEFTIPlatformUrl("http://www.platform.com");
        this.uilDto.setAuthority(authorityDto);

        this.metadataRequestDto.setVehicleID("abc123");
        this.metadataRequestDto.setVehicleCountry("FR");
        this.metadataRequestDto.setAuthority(authorityDto);
        this.metadataRequestDto.setTransportMode("ROAD");

        this.controlDto.setEftiDataUuid(uilDto.getEFTIDataUuid());
        this.controlDto.setEftiGateUrl(uilDto.getEFTIGateUrl());
        this.controlDto.setEftiPlatformUrl(uilDto.getEFTIPlatformUrl());
        this.controlDto.setRequestUuid(requestUuid);
        this.controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH.toString());
        this.controlDto.setStatus(status);
        this.controlDto.setSubsetEuRequested("oki");
        this.controlDto.setSubsetMsRequested("oki");
        this.controlDto.setCreatedDate(localDateTime);
        this.controlDto.setLastModifiedDate(localDateTime);
        this.controlDto.setAuthority(AuthorityDto.builder()
                .country("FR")
                .isEmergencyService(true)
                .legalContact(ContactInformationDto.builder().build())
                .workingContact(ContactInformationDto.builder().build())
                .nationalUniqueIdentifier("unique").build());

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


        metadataResult.setCountryStart("FR");
        metadataResult.setCountryEnd("FR");
        metadataResult.setDisabled(false);
        metadataResult.setDangerousGoods(true);

        metadataResults.setMetadataResult(Collections.singletonList(metadataResult));

        requestEntity.setControl(controlEntity);


        transportVehicleDto.setCountryStart("FR");
        transportVehicleDto.setCountryEnd("FR");
        transportVehicleDto.setTransportMode("ROAD");
        transportVehicleDto.setJourneyStart(LocalDateTime.now().toString());
        transportVehicleDto.setJourneyStart(LocalDateTime.now().plusHours(5L).toString());

        metadataDto.setDangerousGoods(true);
        metadataDto.setMetadataUUID(metadataUuid);
        metadataDto.setDisabled(false);
        metadataDto.setCountryStart("FR");
        metadataDto.setCountryEnd("FR");
        metadataDto.setTransportVehicles(Collections.singletonList(transportVehicleDto));

    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void getByIdWithDataTest() {
        when(controlRepository.findById(1L)).thenReturn(Optional.of(new ControlEntity()));

        ControlEntity controlEntity = controlService.getById(1L);

        verify(controlRepository, times(1)).findById(1L);
        assertNotNull(controlEntity);
    }

    @Test
    void createControlEntityTest() {
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(defaultUilSearchRequestService, times(1)).createAndSendRequest(any());
        verify(controlRepository, times(1)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertNull(requestUuidDtoResult.getErrorCode());
        assertNull(requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorGateNullTest() {
        uilDto.setEFTIGateUrl(null);
        controlDto.setEftiGateUrl(null);
        controlEntity.setStatus(StatusEnum.ERROR.name());
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(defaultUilSearchRequestService, never()).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_GATE_MISSING.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Missing parameter eFTIGateUrl", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorGateFormatTest() {
        uilDto.setEFTIGateUrl("toto");
        controlEntity.setStatus(StatusEnum.ERROR.name());
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(defaultUilSearchRequestService, never()).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_GATE_INCORRECT_FORMAT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Gate format incorrect.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorPlatformNullTest() {
        uilDto.setEFTIPlatformUrl(null);
        controlDto.setEftiPlatformUrl(null);
        controlEntity.setStatus(StatusEnum.ERROR.name());
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(defaultUilSearchRequestService, never()).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_PLATFORM_MISSING.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Missing parameter eFTIPlatformUrl", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorPlatformFormatTest() {
        uilDto.setEFTIPlatformUrl("toto");
        controlEntity.setStatus(StatusEnum.ERROR.name());
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(defaultUilSearchRequestService, never()).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_PLATFORM_INCORRECT_FORMAT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Platform format incorrect.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorUuidNullTest() {
        uilDto.setEFTIDataUuid(null);
        controlDto.setRequestUuid(null);
        controlEntity.setStatus(StatusEnum.ERROR.name());
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(defaultUilSearchRequestService, never()).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_UUID_MISSING.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Missing parameter eFTIDataUuid", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorUuidFormatTest() {
        uilDto.setEFTIDataUuid("toto");
        controlEntity.setStatus(StatusEnum.ERROR.name());
        when(controlRepository.save(any())).thenReturn(controlEntity);
        when(defaultUilSearchRequestService.support(any())).thenReturn(true);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(defaultUilSearchRequestService, never()).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_UUID_INCORRECT_FORMAT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Uuid format incorrect.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void getControlEntitySuccessTest() {
        setField(controlService,"timeoutValue", 60);
        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));
        when(defaultMetadataSearchRequestService.support(any())).thenReturn(true);

        RequestUuidDto requestUuidDtoResult = controlService.getControlEntity(requestUuid);

        verify(controlRepository, times(1)).findByRequestUuid(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(requestUuidDtoResult.getRequestUuid(), controlEntity.getRequestUuid());
    }

    @Test
    void getControlEntitySuccessTestWhenStatusComplete() {
        controlEntity.setStatus(StatusEnum.COMPLETE.name());
        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));

        RequestUuidDto requestUuidDtoResult = controlService.getControlEntity(requestUuid);

        verify(controlRepository, times(1)).findByRequestUuid(any());
        verify(defaultMetadataSearchRequestService, never()).setDataFromRequests(any());
        verify(defaultUilSearchRequestService, never()).setDataFromRequests(any());

        assertNotNull(requestUuidDtoResult);
        assertEquals(requestUuidDtoResult.getRequestUuid(), controlEntity.getRequestUuid());
    }

    @Test
    void shouldUpdatePendingControl_whenMetadataSearchAndRequestsContainsData() {
        //Arrange
        controlEntity.setRequestType(RequestTypeEnum.METADATA_SEARCH.name());
        controlEntity.setRequests(Collections.singletonList(requestEntity));

        requestEntity.setMetadataResults(metadataResults);

        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));
        when(defaultMetadataSearchRequestService.allRequestsContainsData(any())).thenReturn(true);
        when(controlRepository.save(controlEntity)).thenReturn(controlEntity);
        when(defaultMetadataSearchRequestService.support(any())).thenReturn(true);

        //Act
        controlService.getControlEntity(requestUuid);

        assertEquals(StatusEnum.COMPLETE.name(), controlEntity.getStatus());

        verify(controlRepository, times(1)).save(controlEntity);
        verify(controlRepository, times(1)).findByRequestUuid(any());
        verify(defaultMetadataSearchRequestService, times(1)).allRequestsContainsData(controlEntity.getRequests());
        verify(defaultUilSearchRequestService, never()).allRequestsContainsData(any());
    }

    @Test
    void shouldUpdatePendingControl_whenUilSearchAndRequestsContainsData() {
        //Arrange
        controlEntity.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH.name());
        controlEntity.setRequests(Collections.singletonList(requestEntity));

        byte[] data = {10, 20, 30, 40};
        requestEntity.setReponseData(data);

        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));
        when(defaultUilSearchRequestService.allRequestsContainsData(any())).thenReturn(true);
        when(controlRepository.save(controlEntity)).thenReturn(controlEntity);
        when(defaultUilSearchRequestService.support(any())).thenReturn(true);

        //Act
        controlService.getControlEntity(requestUuid);

        //Assert
        assertEquals(StatusEnum.COMPLETE.name(), controlEntity.getStatus());

        verify(controlRepository, times(1)).save(controlEntity);
        verify(controlRepository, times(1)).findByRequestUuid(any());
        verify(defaultUilSearchRequestService, times(1)).allRequestsContainsData(controlEntity.getRequests());
        verify(defaultMetadataSearchRequestService, never()).allRequestsContainsData(any());
    }



    @Test
    void getControlEntityNotFoundTest() {
        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.empty());

        RequestUuidDto requestUuidDtoResult = controlService.getControlEntity(requestUuid);

        verify(controlRepository, times(1)).findByRequestUuid(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals("ERROR", requestUuidDtoResult.getStatus());
        assertNull(requestUuidDtoResult.getEFTIData());
    }

    @Test
    void getControlEntityForMetadataSuccessTestWithoutMetadata() {
        setField(controlService,"timeoutValue", 60);
        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));
        when(defaultMetadataSearchRequestService.support(any())).thenReturn(true);

        MetadataResponseDto metadataResponseDto = controlService.getControlEntityForMetadata(requestUuid);

        verify(controlRepository, times(1)).findByRequestUuid(any());
        assertNotNull(metadataResponseDto);
        assertEquals(metadataResponseDto.getRequestUuid(), controlEntity.getRequestUuid());
    }

    @Test
    void getControlEntityForMetadataSuccessTestWithMetadataPresentInRegistry() {
        setField(controlService,"timeoutValue", 60);
        controlEntity.setMetadataResults(metadataResults);
        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));
        when(metadataService.search(metadataRequestDto)).thenReturn(Collections.singletonList(metadataDto));
        when(defaultMetadataSearchRequestService.support(any())).thenReturn(true);
        MetadataResponseDto metadataResponseDto = controlService.getControlEntityForMetadata(requestUuid);

        verify(controlRepository, times(1)).findByRequestUuid(any());
        assertNotNull(metadataResponseDto);
        assertEquals(metadataResponseDto.getRequestUuid(), controlEntity.getRequestUuid());
    }

    @Test
    void getControlEntityForMetadataNotFoundTest() {
        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.empty());

        MetadataResponseDto metadataResponseDto = controlService.getControlEntityForMetadata(requestUuid);

        verify(controlRepository, times(1)).findByRequestUuid(any());
        assertNotNull(metadataResponseDto);
        assertEquals("ERROR", metadataResponseDto.getStatus());
        assertTrue(metadataResponseDto.getMetadata().isEmpty());
        assertNull(metadataResponseDto.getEFTIGate());
    }

    @Test
    void shouldSetError() {
        final String description = "description";
        final String code = "code";
        final ErrorDto errorDto = ErrorDto.builder()
                .errorDescription(description)
                .errorCode(code).build();
        final ArgumentCaptor<ControlEntity> argumentCaptor = ArgumentCaptor.forClass(ControlEntity.class);
        when(controlRepository.save(any())).thenReturn(controlEntity);

        controlService.setError(controlDto, errorDto);

        verify(controlRepository).save(argumentCaptor.capture());
        assertEquals(StatusEnum.ERROR.name(), argumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldSetData() {
        final ArgumentCaptor<ControlEntity> argumentCaptor = ArgumentCaptor.forClass(ControlEntity.class);
        when(controlRepository.save(any())).thenReturn(controlEntity);

        final String datas = "les grosses datas";
        controlService.setEftiData(controlDto, datas.getBytes(StandardCharsets.UTF_8));

        verify(controlRepository).save(argumentCaptor.capture());
        assertEquals(datas, new String(argumentCaptor.getValue().getEftiData(), StandardCharsets.UTF_8));
        assertEquals(StatusEnum.COMPLETE.name(), argumentCaptor.getValue().getStatus());
    }

    @Test
    void createMetadataControlTest() {
        when(controlRepository.save(any())).thenReturn(controlEntity);
        when(defaultMetadataSearchRequestService.support(any())).thenReturn(true);

        RequestUuidDto requestUuidDtoResult = controlService.createMetadataControl(metadataRequestDto);

        verify(defaultUilSearchRequestService, times(0)).createAndSendRequest(any());
        verify(eftiAsyncCallsProcessor, times(1)).checkLocalRepoAsync(any(), any());
        verify(controlRepository, times(1)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertNull(requestUuidDtoResult.getErrorCode());
        assertNull(requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createMetadataControlWithMinimumRequiredTest() {
        metadataRequestDto.setTransportMode(null);
        metadataRequestDto.setVehicleCountry(null);
        metadataRequestDto.setEFTIGateIndicator(null);
        metadataRequestDto.setIsDangerousGoods(null);

        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createMetadataControl(metadataRequestDto);

        verify(defaultUilSearchRequestService, times(0)).createAndSendRequest(any());
        verify(controlRepository, times(1)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertNull(requestUuidDtoResult.getErrorCode());
        assertNull(requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createMetadataControlVehicleIDIncorrect() {
        metadataRequestDto.setVehicleID("fausse plaque");
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createMetadataControl(metadataRequestDto);

        verify(defaultUilSearchRequestService, times(0)).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.VEHICLE_ID_INCORRECT_FORMAT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals(ErrorCodesEnum.VEHICLE_ID_INCORRECT_FORMAT.getMessage(),requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createMetadataControlVehicleCountryIncorrect() {
        metadataRequestDto.setVehicleCountry("toto");
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createMetadataControl(metadataRequestDto);

        verify(defaultUilSearchRequestService, times(0)).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.VEHICLE_COUNTRY_INCORRECT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals(ErrorCodesEnum.VEHICLE_COUNTRY_INCORRECT.getMessage(),requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createMetadataControlTransportModeIncorrect() {
        metadataRequestDto.setTransportMode("toto");
        when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createMetadataControl(metadataRequestDto);

        verify(defaultUilSearchRequestService, times(0)).createAndSendRequest(any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.TRANSPORT_MODE_INCORRECT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals(ErrorCodesEnum.TRANSPORT_MODE_INCORRECT.getMessage(),requestUuidDtoResult.getErrorDescription());
    }
}
