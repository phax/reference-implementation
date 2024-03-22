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
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.service.gate.EftiGateUrlResolver;
import com.ingroupe.efti.eftigate.service.request.MetadataRequestService;
import com.ingroupe.efti.eftigate.service.request.RequestServiceFactory;
import com.ingroupe.efti.eftigate.service.request.UilRequestService;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class ControlServiceTest extends AbstractServiceTest {

    AutoCloseable openMocks;
    @Mock
    private ControlRepository controlRepository;
    @Mock
    private UilRequestService uilRequestService;

    @Mock
    private MetadataRequestService metadataRequestService;
    @InjectMocks
    private ControlService controlService;
    @Mock
    private MetadataService metadataService;

    @Mock
    private EftiGateUrlResolver eftiGateUrlResolver;

    @Mock
    private RequestServiceFactory requestServiceFactory;

    @Mock
    private EftiAsyncCallsProcessor eftiAsyncCallsProcessor;

    @Mock
    private Function<List<String>, RequestTypeEnum> gateToRequestTypeFunction;


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
        final GateProperties gateProperties = GateProperties.builder()
                .owner("france").build();
        controlService = new ControlService(controlRepository, eftiGateUrlResolver, mapperUtils, requestServiceFactory, gateToRequestTypeFunction, eftiAsyncCallsProcessor, gateProperties);

        //gateToRequestTypeFunction = mock(Function.class);
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
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(uilRequestService);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(uilRequestService, times(1)).createAndSendRequest(any(), any());
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

        verify(uilRequestService, never()).createAndSendRequest(any(), any());
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

        verify(uilRequestService, never()).createAndSendRequest(any(), any());
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

        verify(uilRequestService, never()).createAndSendRequest(any(), any());
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

        verify(uilRequestService, never()).createAndSendRequest(any(), any());
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

        verify(uilRequestService, never()).createAndSendRequest(any(), any());
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
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(uilRequestService);

        RequestUuidDto requestUuidDtoResult = controlService.createUilControl(uilDto);

        verify(uilRequestService, never()).createAndSendRequest(any(), any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_UUID_INCORRECT_FORMAT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Uuid format incorrect.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void getControlEntitySuccessTest() {
        setField(controlService,"timeoutValue", 60);
        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(uilRequestService);

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
        verify(metadataRequestService, never()).setDataFromRequests(any());
        verify(uilRequestService, never()).setDataFromRequests(any());

        assertNotNull(requestUuidDtoResult);
        assertEquals(requestUuidDtoResult.getRequestUuid(), controlEntity.getRequestUuid());
    }

    @Test
    void shouldUpdatePendingControl_whenMetadataSearchAndRequestsContainsData() {
        //Arrange
        controlEntity.setRequestType(RequestTypeEnum.LOCAL_METADATA_SEARCH.name());
        controlEntity.setRequests(Collections.singletonList(requestEntity));

        requestEntity.setMetadataResults(metadataResults);

        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));
        when(metadataRequestService.allRequestsContainsData(any())).thenReturn(true);
        when(controlRepository.save(controlEntity)).thenReturn(controlEntity);
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(metadataRequestService);

        //Act
        controlService.getControlEntity(requestUuid);

        assertEquals(StatusEnum.COMPLETE.name(), controlEntity.getStatus());

        verify(controlRepository, times(1)).save(controlEntity);
        verify(controlRepository, times(1)).findByRequestUuid(any());
        verify(metadataRequestService, times(1)).allRequestsContainsData(controlEntity.getRequests());
        verify(uilRequestService, never()).allRequestsContainsData(any());
    }

    @Test
    void shouldUpdatePendingControl_whenUilSearchAndRequestsContainsData() {
        //Arrange
        controlEntity.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH.name());
        controlEntity.setRequests(Collections.singletonList(requestEntity));

        byte[] data = {10, 20, 30, 40};
        requestEntity.setReponseData(data);

        when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));
        when(uilRequestService.allRequestsContainsData(any())).thenReturn(true);
        when(controlRepository.save(controlEntity)).thenReturn(controlEntity);
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(uilRequestService);

        //Act
        controlService.getControlEntity(requestUuid);

        //Assert
        assertEquals(StatusEnum.COMPLETE.name(), controlEntity.getStatus());

        verify(controlRepository, times(1)).save(controlEntity);
        verify(controlRepository, times(1)).findByRequestUuid(any());
        verify(uilRequestService, times(1)).allRequestsContainsData(controlEntity.getRequests());
        verify(metadataRequestService, never()).allRequestsContainsData(any());
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
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(metadataRequestService);

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
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(metadataRequestService);
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
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(metadataRequestService);
        when(gateToRequestTypeFunction.apply(any())).thenReturn(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH);
        when(eftiGateUrlResolver.resolve(any())).thenReturn(List.of("http://efti.gate.borduria.eu"));


        RequestUuidDto requestUuidDtoResult = controlService.createMetadataControl(metadataRequestDto);
        verify(uilRequestService, never()).createAndSendRequest(any(), any());
        verify(metadataRequestService, times(1)).createAndSendRequest(any(), any());
        verify(eftiAsyncCallsProcessor, never()).checkLocalRepoAsync(any(), any());
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
        when(gateToRequestTypeFunction.apply(any())).thenReturn(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH);
        when(eftiGateUrlResolver.resolve(any())).thenReturn(List.of("http://efti.gate.borduria.eu"));
        when(requestServiceFactory.getRequestServiceByRequestType(any())).thenReturn(metadataRequestService);


        RequestUuidDto requestUuidDtoResult = controlService.createMetadataControl(metadataRequestDto);

        verify(metadataRequestService, times(1)).createAndSendRequest(any(), any());
        verify(uilRequestService, times(0)).createAndSendRequest(any(), any());
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

        verify(uilRequestService, times(0)).createAndSendRequest(any(), any());
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

        verify(uilRequestService, times(0)).createAndSendRequest(any(), any());
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

        verify(uilRequestService, times(0)).createAndSendRequest(any(), any());
        verify(controlRepository, times(0)).save(any());
        assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.TRANSPORT_MODE_INCORRECT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals(ErrorCodesEnum.TRANSPORT_MODE_INCORRECT.getMessage(),requestUuidDtoResult.getErrorDescription());
    }
}
