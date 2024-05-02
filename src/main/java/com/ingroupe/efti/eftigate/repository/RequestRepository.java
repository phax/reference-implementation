package com.ingroupe.efti.eftigate.repository;

import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    RequestEntity findByControlRequestUuidAndStatus(final String EftiDataUuid, final RequestStatusEnum status);

    RequestEntity findByEdeliveryMessageId(final String messageId);

    RequestEntity findByControlRequestTypeInAndEdeliveryMessageId(final List<RequestTypeEnum> controlRequestTypeIn, final String messageId);
}
