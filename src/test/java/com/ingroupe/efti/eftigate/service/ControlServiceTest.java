package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.eftigate.dto.AuthorityDto;
import com.ingroupe.efti.eftigate.dto.ContactInformationDto;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.utils.RequestTypeEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControlServiceTest extends AbstractServceTest {

    AutoCloseable openMocks;
    @Mock
    private ControlRepository controlRepository;
    @Mock
    private RequestService requestService;
    private ControlService controlService;

    private final UilDto uilDto = new UilDto();
    private final ControlDto controlDto = new ControlDto();
    private final ControlEntity controlEntity = new ControlEntity();
    private final RequestUuidDto requestUuidDto = new RequestUuidDto();
    private final String requestUuid = UUID.randomUUID().toString();

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);
        controlService = new ControlService(controlRepository, mapperUtils, requestService);

        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        String status = StatusEnum.PENDING.toString();

        requestUuidDto.setRequestUuid(requestUuid);
        requestUuidDto.setStatus(status);

        this.uilDto.setEFTIGateUrl("http://www.gate.com");
        this.uilDto.setEFTIDataUuid("12345678-ab12-4ab6-8999-123456789abc");
        this.uilDto.setEFTIPlatformUrl("http://www.platform.com");
        this.uilDto.setAuthority(AuthorityDto.builder()
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
                .isEmergencyService(true).build());
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
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void getByIdWithDataTest() {
        Mockito.when(controlRepository.findById(1L)).thenReturn(Optional.of(new ControlEntity()));

        ControlEntity controlEntity = controlService.getById(1L);

        verify(controlRepository, Mockito.times(1)).findById(1L);
        Assertions.assertNotNull(controlEntity);
    }

    @Test
    void createControlEntityTest() {
        Mockito.when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createControlEntity(uilDto);

        verify(requestService, times(1)).createAndSendRequest(any());
        verify(controlRepository, Mockito.times(1)).save(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertNull(requestUuidDtoResult.getErrorCode());
        assertNull(requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorGateNullTest() {
        uilDto.setEFTIGateUrl(null);
        controlDto.setEftiGateUrl(null);
        controlEntity.setStatus(StatusEnum.ERROR.name());
        Mockito.when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createControlEntity(uilDto);

        verify(requestService, never()).createAndSendRequest(any());
        verify(controlRepository, Mockito.times(1)).save(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_GATE_EMPTY.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Gate should not be empty.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorGateFormatTest() {
        uilDto.setEFTIGateUrl("toto");
        controlEntity.setStatus(StatusEnum.ERROR.name());
        Mockito.when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createControlEntity(uilDto);

        verify(requestService, never()).createAndSendRequest(any());
        verify(controlRepository, Mockito.times(1)).save(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_GATE_INCORRECT_FORMAT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Gate format incorrect.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorPlatformNullTest() {
        uilDto.setEFTIPlatformUrl(null);
        controlDto.setEftiPlatformUrl(null);
        controlEntity.setStatus(StatusEnum.ERROR.name());
        Mockito.when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createControlEntity(uilDto);

        verify(requestService, never()).createAndSendRequest(any());
        verify(controlRepository, Mockito.times(1)).save(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_PLATFORM_EMPTY.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Platform should not be empty.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorPlatformFormatTest() {
        uilDto.setEFTIPlatformUrl("toto");
        controlEntity.setStatus(StatusEnum.ERROR.name());
        Mockito.when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createControlEntity(uilDto);

        verify(requestService, never()).createAndSendRequest(any());
        verify(controlRepository, Mockito.times(1)).save(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_PLATFORM_INCORRECT_FORMAT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Platform format incorrect.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorUuidNullTest() {
        uilDto.setEFTIDataUuid(null);
        controlDto.setRequestUuid(null);
        controlEntity.setStatus(StatusEnum.ERROR.name());
        Mockito.when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createControlEntity(uilDto);

        verify(requestService, never()).createAndSendRequest(any());
        verify(controlRepository, Mockito.times(1)).save(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_UUID_EMPTY.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Uuid should not be empty.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void createControlEntityErrorUuidFormatTest() {
        uilDto.setEFTIDataUuid("toto");
        controlEntity.setStatus(StatusEnum.ERROR.name());
        Mockito.when(controlRepository.save(any())).thenReturn(controlEntity);

        RequestUuidDto requestUuidDtoResult = controlService.createControlEntity(uilDto);

        verify(requestService, never()).createAndSendRequest(any());
        verify(controlRepository, Mockito.times(1)).save(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertEquals(ErrorCodesEnum.UIL_UUID_INCORRECT_FORMAT.name(), requestUuidDtoResult.getErrorCode());
        assertEquals("Uuid format incorrect.", requestUuidDtoResult.getErrorDescription());
    }

    @Test
    void getControlEntitySucessTest() {
        Mockito.when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.of(controlEntity));

        RequestUuidDto requestUuidDtoResult = controlService.getControlEntity(requestUuid);

        verify(controlRepository, Mockito.times(1)).findByRequestUuid(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertEquals(requestUuidDtoResult.getRequestUuid(), controlEntity.getRequestUuid());
    }

    @Test
    void getControlEntityNotFoundTest() {
        Mockito.when(controlRepository.findByRequestUuid(any())).thenReturn(Optional.empty());

        RequestUuidDto requestUuidDtoResult = controlService.getControlEntity(requestUuid);

        verify(controlRepository, Mockito.times(1)).findByRequestUuid(any());
        Assertions.assertNotNull(requestUuidDtoResult);
        assertEquals("ERROR", requestUuidDtoResult.getStatus());
        Assertions.assertNull(requestUuidDtoResult.getEFTIData());
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
}
