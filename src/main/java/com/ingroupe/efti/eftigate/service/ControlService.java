package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ControlService {

    private final ControlRepository controlRepository;
    private MapperUtils mapperUtils;
    private RequestService requestService;

    public ControlEntity getById(long id) {
        Optional<ControlEntity> controlEntity = controlRepository.findById(id);
        return controlEntity.orElse(null);
    }

    public ControlEntity createControlEntity(UilDto uilDto) {
        ControlDto controlDto = new ControlDto(uilDto);

        ControlEntity controlEntity = controlRepository.save(mapperUtils.controlDtoToControEntity(controlDto));
        requestService.createRequestEntity(controlEntity);
        return controlEntity;
    }
}
