package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.TransportVehicleDto;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.metadataregistry.entity.TransportVehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MetadataLocalRequestServiceTest extends RequestServiceTest {

    private MetadataLocalRequestService metadataLocalRequestService;
    @Mock
    private MapperUtils mapperUtils;
    @Captor
    ArgumentCaptor<RequestDto> requestDtoArgumentCaptor;

    public static final String DATA_UUID = "12345678-ab12-4ab6-8999-123456789abc";
    public static final String PLATFORM_URL = "http://efti.platform.truc.eu";
    private MetadataDto metadataDto;
    private MetadataResult metadataResult;

    @Override
    @BeforeEach
    public void before() {
       super.before();
       metadataLocalRequestService = new MetadataLocalRequestService(getRequestRepository(), mapperUtils);
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
    }


    @Test
    void shouldCreateRequest() {
        //Arrange
        when(getRequestRepository().save(any())).thenReturn(getRequestEntity());
        when(mapperUtils.requestDtoToRequestEntity(any())).thenReturn(getRequestEntity());
        when(mapperUtils.requestDtoToRequestEntity(any())).thenReturn(getRequestEntity());
        when(mapperUtils.metadataDtosToMetadataEntities(anyList())).thenReturn(List.of(metadataResult));

        //Act
        metadataLocalRequestService.createRequest(getControlDto(), SUCCESS.name(), Collections.singletonList(metadataDto));

        //Assert
        verify(mapperUtils).requestDtoToRequestEntity(requestDtoArgumentCaptor.capture());
        verify(getRequestRepository(), times(1)).save(any());
        assertEquals(List.of(metadataResult), requestDtoArgumentCaptor.getValue().getMetadataResults().getMetadataResult());
        assertEquals("SUCCESS", requestDtoArgumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldCreateWithErrorStatus_WhenNoMetadata() {
        //Arrange
        when(getRequestRepository().save(any())).thenReturn(getRequestEntity());
        when(mapperUtils.requestDtoToRequestEntity(any())).thenReturn(getRequestEntity());
        when(mapperUtils.requestDtoToRequestEntity(any())).thenReturn(getRequestEntity());

        //Act
        metadataLocalRequestService.createRequest(Collections.emptyList(), getControlDto());

        //Assert
        verify(mapperUtils).requestDtoToRequestEntity(requestDtoArgumentCaptor.capture());
        verify(getRequestRepository(), times(1)).save(any());
        assertEquals("ERROR", requestDtoArgumentCaptor.getValue().getStatus());
    }
}
