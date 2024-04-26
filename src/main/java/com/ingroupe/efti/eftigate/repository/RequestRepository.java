package com.ingroupe.efti.eftigate.repository;

import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    RequestEntity findByControlRequestUuidAndStatus(final String EftiDataUuid, final RequestStatusEnum status);

    RequestEntity findByEdeliveryMessageId(final String messageId);
}
