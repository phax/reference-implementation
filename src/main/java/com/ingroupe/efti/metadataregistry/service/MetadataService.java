package com.ingroupe.efti.metadataregistry.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.metadataregistry.MetadataMapper;
import com.ingroupe.efti.metadataregistry.entity.MetadataEntity;
import com.ingroupe.efti.metadataregistry.repository.MetadataRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MetadataService {

    private final MetadataRepository repository;
    private final MetadataMapper mapper;

    public MetadataDto createOrUpdate(final MetadataDto metadataDto) {
        if(metadataDto.getMetadataUUID() != null) {
            final Optional<MetadataEntity> entityOptional = repository.findByMetadataUUID(metadataDto.getMetadataUUID());
            if(entityOptional.isPresent()) {
                metadataDto.setId(entityOptional.get().getId());
                metadataDto.setMetadataUUID(entityOptional.get().getMetadataUUID());
                log.info("updating metadata for uuid {}", metadataDto.getMetadataUUID());
                return this.save(metadataDto);
            }
        }
        metadataDto.setMetadataUUID(UUID.randomUUID().toString());
        log.info("creating new entry for uuid {}", metadataDto.getMetadataUUID());
        return this.save(metadataDto);
    }

    private MetadataDto save(final MetadataDto metadataDto) {
        return mapper.entityToDto(repository.save(mapper.dtoToEntity(metadataDto)));
    }
}
