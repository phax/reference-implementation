package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.ERROR;

public interface RequestService {

    List<RequestTypeEnum> UIL_SEARCH_TYPE = List.of(RequestTypeEnum.LOCAL_UIL_SEARCH);

    boolean allRequestsContainsData(List<RequestEntity> controlEntityRequests);

    void setDataFromRequests(ControlEntity controlEntity);

    boolean support(final RequestTypeEnum requestType);

    default boolean allRequestsAreInErrorStatus(List<RequestEntity> controlEntityRequests){
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> ERROR.name().equalsIgnoreCase(requestEntity.getStatus()));
    }
}
