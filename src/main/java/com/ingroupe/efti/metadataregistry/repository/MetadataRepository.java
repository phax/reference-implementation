package com.ingroupe.efti.metadataregistry.repository;

import com.ingroupe.efti.metadataregistry.entity.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetadataRepository extends JpaRepository<MetadataEntity, Long> {

    Optional<MetadataEntity> findByMetadataUUID(final String uuid);
}
