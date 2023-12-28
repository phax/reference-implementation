package com.ingroupe.efti.metadataregistry.metadataregistry;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.metadataregistry.entity.MetadataEntity;
import com.ingroupe.efti.metadataregistry.repository.MetadataRepository;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetadataServiceTest extends AbstractServiceTest {

    public static final String GATE_URL = "gateUrl";
    public static final String DATA_UUID = "dateUuid";
    public static final String PLATFORM_URL = "platformUrl";
    public static final String UUID = "uuid123";
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

        metadataDto = MetadataDto.builder()
                .eFTIGateUrl(GATE_URL)
                .eFTIDataUuid(DATA_UUID)
                .eFTIPlatformUrl(PLATFORM_URL).build();

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
        verify(repository, never()).findByMetadataUUID(any());
        assertEquals(DATA_UUID, argumentCaptor.getValue().getEFTIDataUuid());
        assertEquals(PLATFORM_URL, argumentCaptor.getValue().getEFTIPlatformUrl());
        assertEquals(GATE_URL, argumentCaptor.getValue().getEFTIGateUrl());
    }

    @Test
    void shouldCreateIfMetadataUuidNotFound() {
        metadataDto.setMetadataUUID(UUID);
        when(repository.save(any())).thenReturn(metadata);
        when(repository.findByMetadataUUID(UUID)).thenReturn(Optional.empty());
        ArgumentCaptor<MetadataEntity> argumentCaptor = ArgumentCaptor.forClass(MetadataEntity.class);

        service.createOrUpdate(metadataDto);

        verify(repository).save(argumentCaptor.capture());
        verify(repository).findByMetadataUUID(UUID);
        assertEquals(DATA_UUID, argumentCaptor.getValue().getEFTIDataUuid());
        assertEquals(PLATFORM_URL, argumentCaptor.getValue().getEFTIPlatformUrl());
        assertEquals(GATE_URL, argumentCaptor.getValue().getEFTIGateUrl());
    }

    @Test
    void shouldUpdateIfMetadataUuidGiven() {
        metadataDto.setMetadataUUID(UUID);
        when(repository.save(any())).thenReturn(metadata);
        when(repository.findByMetadataUUID(UUID)).thenReturn(Optional.of(MetadataEntity.builder().metadataUUID(UUID).build()));
        ArgumentCaptor<MetadataEntity> argumentCaptor = ArgumentCaptor.forClass(MetadataEntity.class);

        service.createOrUpdate(metadataDto);

        verify(repository).save(argumentCaptor.capture());
        verify(repository).findByMetadataUUID(UUID);
        assertEquals(DATA_UUID, argumentCaptor.getValue().getEFTIDataUuid());
        assertEquals(PLATFORM_URL, argumentCaptor.getValue().getEFTIPlatformUrl());
        assertEquals(GATE_URL, argumentCaptor.getValue().getEFTIGateUrl());
        assertEquals(UUID, argumentCaptor.getValue().getMetadataUUID());
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }
}
