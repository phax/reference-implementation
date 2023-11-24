package com.ingroupe.efti.eftigate.mapper;

import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MapperUtils {

    @Autowired
    private ModelMapper modelMapper;

    public ControlEntity controlDtoToControEntity(ControlDto controlDto) {
        return modelMapper.map(controlDto, ControlEntity.class);
    }

    public RequestEntity requestDtoToRequestEntity(RequestDto requestDto) {
        return modelMapper.map(requestDto, RequestEntity.class);
    }
}
