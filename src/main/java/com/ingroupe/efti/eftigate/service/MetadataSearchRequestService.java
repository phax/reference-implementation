package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;

import java.util.List;

public interface MetadataSearchRequestService extends RequestService{
    void createRequest(ControlDto controlDto, String status, List<MetadataDto> metadataDtoList);

    void setDataFromRequests(ControlEntity controlEntity);

    default boolean support(final RequestTypeEnum requestType) {
        return !UIL_SEARCH_TYPE.contains(requestType);
    }
}
