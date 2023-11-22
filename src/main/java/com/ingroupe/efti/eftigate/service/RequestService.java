package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    private MapperUtils mapperUtils;

    public RequestEntity createRequestEntity(ControlEntity controlEntity) {
        RequestDto requestDto = new RequestDto(controlEntity);
        return requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto));
    }
}
