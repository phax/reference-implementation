package com.ingroupe.efti.metadataregistry.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.metadataregistry.MetadataMapper;
import com.ingroupe.efti.metadataregistry.entity.MetadataEntity;
import com.ingroupe.efti.metadataregistry.exception.InvalidMetadataException;
import com.ingroupe.efti.metadataregistry.repository.MetadataRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataService {

    private final MetadataRepository repository;
    private final MetadataMapper mapper;

    @Value("${gate.owner}")
    private String gateFrom;

    public MetadataDto createOrUpdate(final MetadataDto metadataDto) {

        this.enrichAndValidate(metadataDto);

        final Optional<MetadataEntity> entityOptional = repository.findByUil(metadataDto.getEFTIGateUrl(),
                metadataDto.getEFTIDataUuid(), metadataDto.getEFTIPlatformUrl());

        if(entityOptional.isPresent()) {
            metadataDto.setId(entityOptional.get().getId());
            metadataDto.setMetadataUUID(entityOptional.get().getMetadataUUID());
            log.info("updating metadata for uuid {}", metadataDto.getMetadataUUID());
            return this.save(metadataDto);
        }
        metadataDto.setMetadataUUID(UUID.randomUUID().toString());
        log.info("creating new entry for uuid {}", metadataDto.getMetadataUUID());
        return this.save(metadataDto);
    }

    public MetadataDto disable(final MetadataDto metadataDto) {
        metadataDto.setDisabled(true);
        return this.save(metadataDto);
    }

    @Transactional("metadataTransactionManager")
    public List<MetadataDto> search(final MetadataRequestDto metadataRequestDto) {
        return mapper.entityListToDtoList(this.repository.searchByCriteria(metadataRequestDto));
    }

    private void enrichAndValidate(final MetadataDto metadataDto) {
        metadataDto.setEFTIGateUrl(gateFrom);

        final Validator validator;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        final Set<ConstraintViolation<MetadataDto>> violations = validator.validate(metadataDto);
        if(!violations.isEmpty()){
            final String message = String.format("rejecting metadata for uil (gate=%s, uuid=%s, platform=%s) because %s",
                    metadataDto.getEFTIGateUrl(), metadataDto.getEFTIPlatformUrl(), metadataDto.getEFTIPlatformUrl(), violations);
            log.error(message);
            throw new InvalidMetadataException(message);
        }
    }

    private MetadataDto save(final MetadataDto metadataDto) {
        return mapper.entityToDto(repository.save(mapper.dtoToEntity(metadataDto)));
    }
}
