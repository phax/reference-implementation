package com.ingroupe.efti.eftigate.repository;

import com.ingroupe.efti.eftigate.entity.ControlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ControlRepository extends JpaRepository<ControlEntity, Long> {
    Optional<ControlEntity> findByRequestUuid(String requestUuid);
}
