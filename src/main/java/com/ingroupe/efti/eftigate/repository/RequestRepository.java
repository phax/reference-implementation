package com.ingroupe.efti.eftigate.repository;

import com.ingroupe.efti.eftigate.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    RequestEntity findByControlRequestUuidAndStatus(final String EftiDataUuid, final String status);

    RequestEntity findByEdeliveryMessageId(final String messageId);

    @Query(value = "select e from RequestEntity e where e.status = :status and e.nextRetryDate is not null and e.nextRetryDate < :currentDate order by e.nextRetryDate asc limit :querySize")
    List<RequestEntity> getRequestEntitiesByStatus(String status , int querySize, LocalDateTime currentDate);
}
