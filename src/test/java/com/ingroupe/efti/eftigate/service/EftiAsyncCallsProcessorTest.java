package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.AuthorityDto;
import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.TransportVehicleDto;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;

class EftiAsyncCallsProcessorTest {
    AutoCloseable openMocks;
    @Mock
    private MetadataSearchRequestService defaultMetadataSearchRequestService;
    @Mock
    private MetadataService metadataService;

    private EftiAsyncCallsProcessor eftiAsyncCallsProcessor;

    private final MetadataRequestDto metadataRequestDto = new MetadataRequestDto();

    MetadataDto metadataDto= new MetadataDto();

    private final String metadataUuid = UUID.randomUUID().toString();
    TransportVehicleDto transportVehicleDto = new TransportVehicleDto();
    private final ControlDto controlDto = new ControlDto();


    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);
        eftiAsyncCallsProcessor = new EftiAsyncCallsProcessor(defaultMetadataSearchRequestService, metadataService);

        final AuthorityDto authorityDto = new AuthorityDto();


        metadataDto.setDangerousGoods(true);
        metadataDto.setMetadataUUID(metadataUuid);
        metadataDto.setDisabled(false);
        metadataDto.setCountryStart("FR");
        metadataDto.setCountryEnd("FR");
        metadataDto.setTransportVehicles(Collections.singletonList(transportVehicleDto));


        this.metadataRequestDto.setVehicleID("abc123");
        this.metadataRequestDto.setVehicleCountry("FR");
        this.metadataRequestDto.setAuthority(authorityDto);
        this.metadataRequestDto.setTransportMode("ROAD");
    }

        @Test
    void checkLocalRepoTest_whenMetadataIsPresentInRegistry() {
        //Arrange
        when(metadataService.search(metadataRequestDto)).thenReturn(Collections.singletonList(metadataDto));

        //Act
        eftiAsyncCallsProcessor.checkLocalRepoAsync(metadataRequestDto, controlDto);

        //Assert
        verify(defaultMetadataSearchRequestService, times(1)).createRequest(controlDto, "SUCCESS", Collections.singletonList(metadataDto));
    }

    @Test
    void checkLocalRepoTest_whenMetadataIsNotPresentInRegistry() {
        //Arrange

        //Act
        eftiAsyncCallsProcessor.checkLocalRepoAsync(metadataRequestDto, controlDto);

        //Assert
        verify(defaultMetadataSearchRequestService, times(1)).createRequest(controlDto, "ERROR", null);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }
}
