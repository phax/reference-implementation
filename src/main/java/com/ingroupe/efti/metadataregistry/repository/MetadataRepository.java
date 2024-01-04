package com.ingroupe.efti.metadataregistry.repository;

import com.ingroupe.efti.metadataregistry.entity.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MetadataRepository extends JpaRepository<MetadataEntity, Long> {

    @Query(value = "SELECT m FROM MetadataEntity m where m.eFTIGateUrl = :gate and m.eFTIDataUuid = :uuid and m.eFTIPlatformUrl = :platform")
    Optional<MetadataEntity> findByUil(final String gate, final String uuid, final String platform);
}
