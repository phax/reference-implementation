package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;

public interface UilSearchRequestService extends RequestService {

    RequestDto createAndSendRequest(final ControlDto controlDto);

    void sendRequest(final RequestDto requestDto);

    void updateWithResponse(final NotificationDto notificationDto);

    void setDataFromRequests(final ControlEntity controlEntity);

    default boolean support(final RequestTypeEnum requestType) {
        return UIL_SEARCH_TYPE.contains(requestType);
    }
}
