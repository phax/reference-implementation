package eu.efti.eftigate.repository;

import eu.efti.commons.enums.RequestStatusEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.eftigate.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    RequestEntity findByControlRequestUuidAndStatus(final String EftiDataUuid, final RequestStatusEnum status);

    RequestEntity findByEdeliveryMessageId(final String messageId);

    RequestEntity findByControlRequestTypeInAndStatusAndEdeliveryMessageId(final List<RequestTypeEnum> controlRequestTypeIn, final RequestStatusEnum requestStatusEnum, final String messageId);
}
