package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.utils.ErrorCodesEnum;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        this.uilDto.setGate("http://www.gate.com");
        this.uilDto.setUuid("12345678-ab12-4ab6-8999-123456789abc");
        this.uilDto.setPlatform("http://www.platform.com");
        this.controlDto.setEftiDataUuid(uilDto.getUuid());
        this.controlDto.setEftiGateUrl(uilDto.getGate());
        this.controlDto.setEftiPlatformUrl(uilDto.getPlatform());
        this.controlDto.setRequestUuid(requestUuid);
        this.controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH.toString());
        this.controlDto.setStatus(status);
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
        uilDto.setGate(null);
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
        uilDto.setGate("toto");
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
        uilDto.setPlatform(null);
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
        uilDto.setPlatform("toto");
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
        uilDto.setUuid(null);
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
        uilDto.setUuid("toto");
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
}
