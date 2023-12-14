package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.utils.ErrorCodesEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    public RequestUuidDto createControlEntity(UilDto uilDto) {
        log.info("create ControlEntity with uuid : {}", uilDto.getUuid());
        ControlDto controlDto = new ControlDto(uilDto);

        final Optional<ErrorDto> errorOptional = this.validateControl(controlDto);
        final ControlDto saveControl = this.save(controlDto, errorOptional);

        log.info("control with uil '{}' has been register", uilDto.getUuid());

        errorOptional.ifPresentOrElse(a -> {} , () -> requestService.createAndSendRequest(saveControl));

        return buildResponse(controlDto);
    }

    public RequestUuidDto getControlEntity(final String requestUuid) {
        log.info("get ControlEntity with uuid : {}", requestUuid);
        final Optional<ControlEntity> optionalControlEntity = controlRepository.findByRequestUuid(requestUuid);
        final ControlDto controlDto = mapperUtils.controlEntityToControlDto(optionalControlEntity.orElse(
                ControlEntity.builder()
                        .status(StatusEnum.ERROR.name())
                        .error(ErrorEntity.builder()
                                .errorCode(ErrorCodesEnum.UUID_NOT_FOUND.name())
                                .errorDescription("Error requestUuid not found.").build()).build()));
        return buildResponse(controlDto);
    }

    private ControlDto save(final ControlDto controlDto) {
        return mapperUtils.controlEntityToControlDto(
                controlRepository.save(mapperUtils.controlDtoToControEntity(controlDto)));
    }

    private ControlDto save(final ControlDto controlDto, final Optional<ErrorDto> errorDto) {
        errorDto.ifPresent(error -> {
                controlDto.setError(error);
                controlDto.setStatus(StatusEnum.ERROR.name());
        });
        return this.save(controlDto);
    }

    private Optional<ErrorDto> validateControl(final ControlDto controlDto) {
        if(StringUtils.isBlank(controlDto.getEftiGateUrl())) {
            return Optional.of(ErrorDto.builder()
                    .errorCode(ErrorCodesEnum.UIL_GATE_EMPTY.name())
                    .errorDescription("The gate identifier is empty.").build());
        }
        if(StringUtils.isBlank(controlDto.getEftiPlatformUrl())) {
            return Optional.of(ErrorDto.builder()
                    .errorCode(ErrorCodesEnum.UIL_PLATFORM_EMPTY.name())
                    .errorDescription("The platform identifier is empty.").build());
        }
        if(StringUtils.isBlank(controlDto.getEftiDataUuid())) {
            return Optional.of(ErrorDto.builder()
                    .errorCode(ErrorCodesEnum.UIL_UUID_EMPTY.name())
                    .errorDescription("The request uuid is empty.").build());
        }
        return Optional.empty();
    }

    private RequestUuidDto buildResponse(final ControlDto controlDto) {
        final RequestUuidDto result = RequestUuidDto.builder()
                .requestUuid(controlDto.getRequestUuid())
                .status(controlDto.getStatus())
                .eFTIData(controlDto.getEftiData()).build();
        if(controlDto.isError()) {
            result.setErrorDescription(controlDto.getError().getErrorDescription());
            result.setErrorCode(controlDto.getError().getErrorCode());
        }
        return result;
    }

}
