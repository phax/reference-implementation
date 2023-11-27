package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ControlService {

    private final ControlRepository controlRepository;
    private MapperUtils mapperUtils;
    private RequestService requestService;

    public ControlEntity getById(long id) {
        Optional<ControlEntity> controlEntity = controlRepository.findById(id);
        return controlEntity.orElse(null);
    }

    @Transactional
    public ControlEntity createControlEntity(UilDto uilDto) {
        log.info("createControlEntity with uuid : {}", uilDto.getUuid());
        ControlDto controlDto = new ControlDto(uilDto);

        ControlEntity controlEntity = controlRepository.save(mapperUtils.controlDtoToControEntity(controlDto));
        log.info("control with uil '{}' has been register", uilDto.getUuid());
        requestService.createRequestEntity(controlEntity);
        return controlEntity;
    }

    public RequestUuidDto getControlEntity(String requestUuid) {
        log.info("getControlEntity with uuid : {}", requestUuid);
        Optional<ControlEntity> optionalControlEntity = controlRepository.findByRequestUuid(requestUuid);
        RequestUuidDto requestUuidDto = new RequestUuidDto();

        if (optionalControlEntity.isPresent()) {
            log.info("Sucess, found controlEntity with {} as uuid", requestUuid);
            ControlEntity controlEntity = optionalControlEntity.get();

            requestUuidDto.setStatus(controlEntity.getStatus());
            requestUuidDto.setRequestUuid(controlEntity.getRequestUuid());
            requestUuidDto.setEFTIData(controlEntity.getEftiData());
        } else {
            log.error("ERROR requestUuid {} not found", requestUuid);
            requestUuidDto.setStatus(StatusEnum.ERROR.toString());
            requestUuidDto.setRequestUuid(requestUuid);
            requestUuidDto.setErrorDescription("Error requestUuid not found");
        }
        return requestUuidDto;
    }
}
