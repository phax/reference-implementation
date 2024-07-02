package com.ingroupe.efti.eftigate.repository;

import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository<T extends RequestEntity> extends JpaRepository<T, Long> {

    T findByEdeliveryMessageId(final String messageId);

    T findByControlRequestTypeAndStatusAndEdeliveryMessageId(final RequestTypeEnum controlRequestType, final RequestStatusEnum requestStatusEnum, final String messageId);
}
