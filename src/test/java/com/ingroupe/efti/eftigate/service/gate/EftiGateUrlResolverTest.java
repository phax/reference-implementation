package com.ingroupe.efti.eftigate.service.gate;

import com.ingroupe.efti.commons.dto.AuthorityDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.eftigate.entity.GateEntity;
import com.ingroupe.efti.eftigate.repository.GateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class EftiGateUrlResolverTest {
    AutoCloseable openMocks;
    private EftiGateUrlResolver eftiGateUrlResolver;
    @Mock
    private GateRepository gateRepository;

    private final MetadataRequestDto metadataRequestDto = new MetadataRequestDto();

    GateEntity frGateEntity;
    GateEntity beGateEntity;
    GateEntity deGateEntity;

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);
        eftiGateUrlResolver = new EftiGateUrlResolver(gateRepository);

        final AuthorityDto authorityDto = new AuthorityDto();

        this.metadataRequestDto.setVehicleID("abc123");
        this.metadataRequestDto.setVehicleCountry("FR");
        this.metadataRequestDto.setAuthority(authorityDto);
        this.metadataRequestDto.setTransportMode("ROAD");

        frGateEntity = GateEntity.builder().id(1L).url("https://efti.gate.fr.eu").country(CountryIndicator.FR).build();
        beGateEntity = GateEntity.builder().id(2L).url("https://efti.gate.be.eu").country(CountryIndicator.BE).build();
        deGateEntity = GateEntity.builder().id(3L).url("https://efti.gate.de.eu").country(CountryIndicator.DE).build();
    }

    @Test
    void shouldResolveUrls_WhenCountryIndicatorsAreGiven(){
        //Arrange
        this.metadataRequestDto.setEFTIGateIndicator(List.of("BE", "FR"));
        when(gateRepository.findByCountryIn(anyList())).thenReturn(List.of(frGateEntity, beGateEntity));

        //Act
        List<String> destinationGatesUrls = eftiGateUrlResolver.resolve(metadataRequestDto);

        //Assert
        assertFalse(destinationGatesUrls.isEmpty());
        assertEquals(List.of("https://efti.gate.fr.eu", "https://efti.gate.be.eu"), destinationGatesUrls);
    }

    @Test
    void shouldGetAllUrls_WhenCountryIndicatorsAreNotGiven(){
        //Arrange
        when(gateRepository.findAll()).thenReturn(List.of(frGateEntity, beGateEntity, deGateEntity));

        //Act
        List<String> destinationGatesUrls = eftiGateUrlResolver.resolve(metadataRequestDto);

        //Assert
        assertFalse(destinationGatesUrls.isEmpty());
        assertEquals(List.of("https://efti.gate.fr.eu", "https://efti.gate.be.eu", "https://efti.gate.de.eu"), destinationGatesUrls);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }
}
