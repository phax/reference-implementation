package com.ingroupe.efti.eftigate.service.impl;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.TransportVehicleDto;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.service.RequestServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DefaultMetadataSearchRequestServiceTest extends RequestServiceTest {

    private DefaultMetadataSearchRequestService defaultMetadataSearchRequestService;

    public static final String DATA_UUID = "12345678-ab12-4ab6-8999-123456789abc";
    public static final String PLATFORM_URL = "http://efti.platform.truc.eu";
    private MetadataDto metadataDto;

    @BeforeEach
    public void before() {
       super.before();
        defaultMetadataSearchRequestService = new DefaultMetadataSearchRequestService(getRequestRepository(), mapperUtils);
        metadataDto = MetadataDto.builder()
                .eFTIDataUuid(DATA_UUID)
                .eFTIPlatformUrl(PLATFORM_URL)
                .transportVehicles(List.of(TransportVehicleDto.builder()
                                .vehicleId("abc123").countryStart("FR").countryEnd("toto").build(),
                        TransportVehicleDto.builder()
                                .vehicleId("abc124").countryStart("osef").countryEnd("IT").build())).build();    }


    @Test
    void createRequestForMetadataTest() {
        //Arrange
        when(getRequestRepository().save(any())).thenReturn(getRequestEntity());
        //Act
        defaultMetadataSearchRequestService.createRequest(getControlDto(), RequestStatusEnum.SUCCESS.name(), Collections.singletonList(metadataDto));
        //Assert
        Mockito.verify(getRequestRepository(), Mockito.times(2)).save(any());
        assertNotNull(getRequestEntity().getMetadataResults());
    }

    @Test
    void allRequestsContainsDataTest_whenFalse() {
        //Arrange
        when(getRequestRepository().save(any())).thenReturn(getRequestEntity());
        //Act and Assert
        assertFalse(defaultMetadataSearchRequestService.allRequestsContainsData(List.of(getRequestEntity())));
    }

    @Test
    void allRequestsContainsDataTest_whenTrue() {
        //Arrange
        getRequestEntity().setMetadataResults(new MetadataResults());
        //Act and Assert
        assertTrue(defaultMetadataSearchRequestService.allRequestsContainsData(List.of(getRequestEntity())));
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
        defaultMetadataSearchRequestService.setDataFromRequests(controlEntity);

        //Assert
        assertNotNull(controlEntity.getMetadataResults());
        assertEquals(2, controlEntity.getMetadataResults().getMetadataResult().size());
    }
}
