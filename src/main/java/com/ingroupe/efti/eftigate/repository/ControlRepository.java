package com.ingroupe.efti.eftigate.repository;

import com.ingroupe.efti.eftigate.entity.ControlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ControlRepository extends JpaRepository<ControlEntity, Long> {
    Optional<ControlEntity> findByRequestUuid(String requestUuid);

    @Query(value = "SELECT * FROM control WHERE status =:status AND createddate > now() - make_interval(0,0,0,0,0,0,:timeoutValue)", nativeQuery = true)
    List<ControlEntity> findByCriteria(String status, Integer timeoutValue);
}
