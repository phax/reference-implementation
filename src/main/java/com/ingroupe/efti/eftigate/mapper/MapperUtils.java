package com.ingroupe.efti.eftigate.mapper;

import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.requestbody.AuthorityBodyDto;
import com.ingroupe.efti.eftigate.dto.AuthorityDto;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapperUtils {

    private final ModelMapper modelMapper;

    public ControlEntity controlDtoToControEntity(final ControlDto controlDto) {
        return modelMapper.map(controlDto, ControlEntity.class);
    }

    public ControlDto controlEntityToControlDto(final ControlEntity controlEntity) {
        return modelMapper.map(controlEntity, ControlDto.class);
    }

    public RequestEntity requestDtoToRequestEntity(final RequestDto requestDto) {
        return modelMapper.map(requestDto, RequestEntity.class);
    }

    public RequestDto requestToRequestDto(final RequestEntity requestEntity) {
        return modelMapper.map(requestEntity, RequestDto.class);
    }

    public AuthorityBodyDto authorityDtoToAuthorityBodyDto(final AuthorityDto authorityDto) {
        return modelMapper.map(authorityDto, AuthorityBodyDto.class);
    }

    public ErrorDto errorDtoToError(final ErrorEntity errorEntity) {
        return modelMapper.map(errorEntity, ErrorDto.class);
    }

    public ErrorEntity errorToErrorDto(final ErrorDto errorDto) {
        return modelMapper.map(errorDto, ErrorEntity.class);
    }
}
