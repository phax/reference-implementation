package com.ingroupe.efti.metadataregistry.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.metadataregistry.entity.MetadataEntity;
import com.ingroupe.efti.metadataregistry.exception.InvalidMetadataException;
import com.ingroupe.efti.metadataregistry.repository.MetadataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetadataServiceTest extends AbstractServiceTest {

    public static final String GATE_URL = "http://efti.gate.borduria.eu";
    public static final String DATA_UUID = "12345678-ab12-4ab6-8999-123456789abc";
    public static final String PLATFORM_URL = "http://efti.platform.truc.eu";
    AutoCloseable openMocks;

    private MetadataService service;
    @Mock
    private MetadataRepository repository;

    private MetadataDto metadataDto;
    private MetadataEntity metadata;

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);
        service = new MetadataService(repository, mapperUtils);

        ReflectionTestUtils.setField(service, "gateFrom", "http://efti.gate.borduria.eu");

        metadataDto = MetadataDto.builder()
                .eFTIDataUuid(DATA_UUID)
                .eFTIPlatformUrl(PLATFORM_URL)
                .transportVehicles(new LinkedList<>()).build();

        metadata =  MetadataEntity.builder()
                .eFTIGateUrl(GATE_URL)
                .eFTIDataUuid(DATA_UUID)
                .eFTIPlatformUrl(PLATFORM_URL).build();
    }

    @Test
    void shouldCreateMetadata() {
        when(repository.save(any())).thenReturn(metadata);
        ArgumentCaptor<MetadataEntity> argumentCaptor = ArgumentCaptor.forClass(MetadataEntity.class);

        service.createOrUpdate(metadataDto);

        verify(repository).save(argumentCaptor.capture());
        assertEquals(DATA_UUID, argumentCaptor.getValue().getEFTIDataUuid());
        assertEquals(PLATFORM_URL, argumentCaptor.getValue().getEFTIPlatformUrl());
        assertEquals(GATE_URL, argumentCaptor.getValue().getEFTIGateUrl());
    }

    @Test
    void shouldCreateIfUilNotFound() {
        when(repository.save(any())).thenReturn(metadata);
        when(repository.findByUil(GATE_URL, DATA_UUID, PLATFORM_URL)).thenReturn(Optional.empty());
        ArgumentCaptor<MetadataEntity> argumentCaptor = ArgumentCaptor.forClass(MetadataEntity.class);

        service.createOrUpdate(metadataDto);

        verify(repository).save(argumentCaptor.capture());
        verify(repository).findByUil(GATE_URL, DATA_UUID, PLATFORM_URL);
        assertEquals(DATA_UUID, argumentCaptor.getValue().getEFTIDataUuid());
        assertEquals(PLATFORM_URL, argumentCaptor.getValue().getEFTIPlatformUrl());
        assertEquals(GATE_URL, argumentCaptor.getValue().getEFTIGateUrl());
    }

    @Test
    void shouldUpdateIfUILFound() {
        when(repository.save(any())).thenReturn(metadata);
        when(repository.findByUil(GATE_URL, DATA_UUID, PLATFORM_URL)).thenReturn(Optional.of(MetadataEntity.builder().build()));
        ArgumentCaptor<MetadataEntity> argumentCaptor = ArgumentCaptor.forClass(MetadataEntity.class);

        service.createOrUpdate(metadataDto);

        verify(repository).save(argumentCaptor.capture());
        verify(repository).findByUil(GATE_URL, DATA_UUID, PLATFORM_URL);
        assertEquals(DATA_UUID, argumentCaptor.getValue().getEFTIDataUuid());
        assertEquals(PLATFORM_URL, argumentCaptor.getValue().getEFTIPlatformUrl());
        assertEquals(GATE_URL, argumentCaptor.getValue().getEFTIGateUrl());
    }

    @Test
    void shouldThrowIfMetadataNotValid() {
        metadataDto.setEFTIDataUuid("wrong");

        assertThrows(InvalidMetadataException.class, () -> service.createOrUpdate(metadataDto));
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }
}
