package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;

    private MapperUtils mapperUtils;

    public RequestEntity createRequestEntity(ControlEntity controlEntity) {
        RequestDto requestDto = new RequestDto(controlEntity);
        log.info("Request has been register with controlId : {}", requestDto.getControlId());
        return requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto));
    }
}
