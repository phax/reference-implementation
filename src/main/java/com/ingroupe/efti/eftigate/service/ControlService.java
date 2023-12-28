package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.ValidableControl;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class ControlService {

    private final ControlRepository controlRepository;
    private final MapperUtils mapperUtils;
    @Lazy
    private final RequestService requestService;

    public ControlEntity getById(long id) {
        Optional<ControlEntity> controlEntity = controlRepository.findById(id);
        return controlEntity.orElse(null);
    }

    @Transactional("controlTransactionManager")
    public RequestUuidDto createUilControl(final UilDto uilDto) {
        log.info("create Uil control for uuid : {}", uilDto.getEFTIDataUuid());
        return createControl(uilDto, ControlDto.fromUilControl(uilDto));
    }

    @Transactional("controlTransactionManager")
    public RequestUuidDto createMetadataControl(final MetadataRequestDto metadataRequestDto) {
        log.info("create metadata control for vehicleId : {}", metadataRequestDto.getVehicleID());
        return createControl(metadataRequestDto, ControlDto.fromMetadataControl(metadataRequestDto));
    }

    private RequestUuidDto createControl(final ValidableControl control, ControlDto controlDto) {
        final Optional<ErrorDto> errorOptional = this.validateControl(control);

        errorOptional.ifPresentOrElse(
                error -> {
                    controlDto.setStatus(StatusEnum.ERROR.name());
                    controlDto.setError(error);
                    log.error(error.getErrorDescription() + ", " + error.getErrorCode());
                },
                () -> {
                    final ControlDto saveControl = this.save(controlDto);
                    //temporaire, request pas implémenté pour les metadata
                    if(control instanceof UilDto) {
                        requestService.createAndSendRequest(saveControl);
                    }
                    log.info("control with request uuid '{}' has been register", saveControl.getRequestUuid());
                });

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

    public ControlDto setError(final ControlDto controlDto, final ErrorDto errorDto) {
        controlDto.setStatus(StatusEnum.ERROR.name());
        controlDto.setError(errorDto);
        return this.save(controlDto);
    }

    public ControlDto setEftiData(final ControlDto controlDto, final byte[] data) {
        controlDto.setStatus(StatusEnum.COMPLETE.name());
        controlDto.setEftiData(data);
        return this.save(controlDto);
    }


    private ControlDto save(final ControlDto controlDto) {
        return mapperUtils.controlEntityToControlDto(
                controlRepository.save(mapperUtils. controlDtoToControEntity(controlDto)));
    }

    private Optional<ErrorDto> validateControl(final UilDto uilDto) {
        final Validator validator;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        final Set<ConstraintViolation<ValidableControl>> violations = validator.validate(validable);

        if(violations.isEmpty()) {
            return Optional.empty();
        }

        //we manage only one error by control
        final ConstraintViolation<ValidableControl> constraintViolation = violations.iterator().next();

        return  Optional.of(ErrorDto.builder()
                .errorCode(constraintViolation.getMessage())
                .errorDescription(ErrorCodesEnum.valueOf(constraintViolation.getMessage()).getMessage())
                .build());
    }

    private RequestUuidDto buildResponse(final ControlDto controlDto) {
        final RequestUuidDto result = RequestUuidDto.builder()
                .requestUuid(controlDto.getRequestUuid())
                .status(controlDto.getStatus())
                .eFTIData(controlDto.getEftiData()).build();
        if(controlDto.isError()) {
            result.setRequestUuid(null);
            result.setErrorDescription(controlDto.getError().getErrorDescription());
            result.setErrorCode(controlDto.getError().getErrorCode());
        }
        return result;
    }

}
