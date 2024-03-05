package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.MetadataResponseDto;
import com.ingroupe.efti.commons.dto.MetadataResultDto;
import com.ingroupe.efti.commons.dto.ValidableControl;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
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
    private final MetadataService metadataService;

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
                    } else if (control instanceof MetadataRequestDto metadataRequestDto) {
                        RequestDto requestForMetadata = requestService.createRequestForMetadata(saveControl);
                        checkLocalRepo(metadataRequestDto, saveControl, requestForMetadata);
                    }
                    log.info("control with request uuid '{}' has been register", saveControl.getRequestUuid());
                });

        return buildResponse(controlDto);
    }

    public RequestUuidDto getControlEntity(final String requestUuid) {
        final ControlDto controlDto = getControlDto(requestUuid);
        return buildResponse(controlDto);
    }

    public MetadataResponseDto getControlEntityForMetadata(String requestUuid) {
        final ControlDto controlDto = getControlDto(requestUuid);
        return buildMetadataResponse(controlDto);
    }

    private MetadataResponseDto buildMetadataResponse(ControlDto controlDto) {
        final MetadataResponseDto result = MetadataResponseDto.builder()
                .requestUuid(controlDto.getRequestUuid())
                .status(controlDto.getStatus())
                .eFTIGate(getEftiGate(controlDto))
                .metadata(getMetadataResultDtos(controlDto)).build();
        if(controlDto.isError()) {
            result.setRequestUuid(null);
            result.setErrorDescription(controlDto.getError().getErrorDescription());
            result.setErrorCode(controlDto.getError().getErrorCode());
        }
        return result;
    }

    private CountryIndicator getEftiGate(ControlDto controlDto) {
        //temporaire en attendant de discuter sur sa place
        MetadataResults metadataResults = controlDto.getMetadataResults();
        if (metadataResults != null && CollectionUtils.isNotEmpty(metadataResults.getMetadataResult())){
            String countryStart = metadataResults.getMetadataResult().iterator().next().getCountryStart();
            if (StringUtils.isNotBlank(countryStart)){
                return CountryIndicator.valueOf(countryStart);
            }
        }
        return null;
    }

    private List<MetadataResultDto> getMetadataResultDtos(ControlDto controlDto) {
        MetadataResults metadataResults = controlDto.getMetadataResults();
        if (metadataResults != null){
            return mapperUtils.metadataResultEntitiesToMetadataResultDtos(metadataResults.getMetadataResult());
        }
        return Collections.emptyList();
    }

    private ControlDto getControlDto(String requestUuid) {
        log.info("get ControlEntity with uuid : {}", requestUuid);
        final Optional<ControlEntity> optionalControlEntity = controlRepository.findByRequestUuid(requestUuid);
        return mapperUtils.controlEntityToControlDto(optionalControlEntity.orElse(
                ControlEntity.builder()
                        .status(StatusEnum.ERROR.name())
                        .error(ErrorEntity.builder()
                                .errorCode(ErrorCodesEnum.UUID_NOT_FOUND.name())
                                .errorDescription("Error requestUuid not found.").build()).build()));
    }

    public void setError(final ControlDto controlDto, final ErrorDto errorDto) {
        controlDto.setStatus(StatusEnum.ERROR.name());
        controlDto.setError(errorDto);
        this.save(controlDto);
    }

    public void setEftiData(final ControlDto controlDto, final byte[] data) {
        controlDto.setStatus(StatusEnum.COMPLETE.name());
        controlDto.setEftiData(data);
        this.save(controlDto);
    }

    @Async
    public void checkLocalRepo(final MetadataRequestDto metadataRequestDto, ControlDto saveControl, RequestDto requestForMetadata) {
        List<MetadataDto> metadataDtoList = metadataService.search(metadataRequestDto);
        if (CollectionUtils.isNotEmpty(metadataDtoList)){
            updateControlWithMetadataList(metadataDtoList, saveControl, requestForMetadata);
        }
        else {
            requestService.updateStatus(requestForMetadata, RequestStatusEnum.ERROR);
        }
    }

    private void updateControlWithMetadataList(List<MetadataDto> metadataDtos, ControlDto saveControl, RequestDto metadataRequestDto) {
        String requestUuid = saveControl.getRequestUuid();
        Optional<ControlEntity> optionalControlEntity = controlRepository.findByRequestUuid(requestUuid);
        optionalControlEntity.ifPresent(controlEntity -> {
            controlEntity.setMetadataResults(buildMetadataResult(metadataDtos));
            controlEntity.setStatus(StatusEnum.COMPLETE.name());
            controlRepository.save(controlEntity);
            requestService.updateStatus(metadataRequestDto, RequestStatusEnum.SUCCESS);
        });
    }

    private MetadataResults buildMetadataResult(List<MetadataDto> metadataDtos) {
        List<MetadataResult> metadataResultList = mapperUtils.metadataDtosToMetadataEntities(metadataDtos);
        return MetadataResults.builder()
                .metadataResult(metadataResultList)
                .build();
    }


    private ControlDto save(final ControlDto controlDto) {
        return mapperUtils.controlEntityToControlDto(
                controlRepository.save(mapperUtils. controlDtoToControEntity(controlDto)));
    }

    private Optional<ErrorDto> validateControl(final ValidableControl validable) {
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
