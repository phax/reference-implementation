package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.MetadataResponseDto;
import com.ingroupe.efti.commons.dto.MetadataResultDto;
import com.ingroupe.efti.commons.dto.ValidableDto;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.service.gate.EftiGateUrlResolver;
import com.ingroupe.efti.eftigate.service.request.RequestService;
import com.ingroupe.efti.eftigate.service.request.RequestServiceFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.ingroupe.efti.commons.enums.ErrorCodesEnum.DATA_NOT_FOUND;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class ControlService {

    private final ControlRepository controlRepository;
    private final EftiGateUrlResolver eftiGateUrlResolver;
    private final MapperUtils mapperUtils;


    private final RequestServiceFactory requestServiceFactory;

    @Value("${efti.control.pending.timeout:60}")
    private Integer timeoutValue;

    private final Function<List<String>, RequestTypeEnum> gateToRequestTypeFunction;

    private final EftiAsyncCallsProcessor eftiAsyncCallsProcessor;

    private final GateProperties gateProperties;

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

    private <T extends ValidableDto> RequestUuidDto createControl(final T searchDto, ControlDto controlDto) {
        final Optional<ErrorDto> errorOptional = this.validateControl(searchDto);
        errorOptional.ifPresentOrElse(error -> createErrorControl(controlDto, error), () -> {
            if(searchDto instanceof UilDto) {
                createUilControl(controlDto);
            } else if (searchDto instanceof MetadataRequestDto metadataRequestDto) {
                createMetadataControl(controlDto, metadataRequestDto);
            }
        });
        return buildResponse(controlDto);
    }

    private void createUilControl(ControlDto controlDto) {
        final ControlDto saveControl = this.save(controlDto);
        getRequestService(controlDto.getRequestType()).createAndSendRequest(saveControl, null);
        log.info("Uil control with request uuid '{}' has been register", saveControl.getRequestUuid());
    }

    private static void createErrorControl(ControlDto controlDto, ErrorDto error) {
        controlDto.setStatus(StatusEnum.ERROR.name());
        controlDto.setError(error);
        log.error(error.getErrorDescription() + ", " + error.getErrorCode());
    }

    private void createMetadataControl(ControlDto controlDto, MetadataRequestDto metadataRequestDto) {
        List<String> destinationGatesUrls = eftiGateUrlResolver.resolve(metadataRequestDto);
        controlDto.setRequestType(gateToRequestTypeFunction.apply(destinationGatesUrls).name());
        final ControlDto saveControl = this.save(controlDto);
            CollectionUtils.emptyIfNull(destinationGatesUrls).forEach(destinationUrl -> {
                if (destinationUrl.equalsIgnoreCase(gateProperties.getOwner())){
                    eftiAsyncCallsProcessor.checkLocalRepoAsync(metadataRequestDto, saveControl);
                } else {
                    getRequestService(saveControl.getRequestType()).createAndSendRequest(saveControl, destinationUrl);
                }
            });
        log.info("Metadata control with request uuid '{}' has been register", saveControl.getRequestUuid());
    }

    public RequestUuidDto getControlEntity(final String requestUuid) {
        final ControlDto controlDto = getControlDto(requestUuid);
        return buildResponse(controlDto);
    }

    public MetadataResponseDto getControlEntityForMetadata(String requestUuid) {
        final ControlDto controlDto = getControlDto(requestUuid);
        return buildMetadataResponse(controlDto);
    }

    public int updatePendingControls(){
        List<ControlEntity> pendingControls = controlRepository.findByCriteria(StatusEnum.PENDING.name(), timeoutValue);
        CollectionUtils.emptyIfNull(pendingControls).forEach(this::updatePendingControl);
        return CollectionUtils.isNotEmpty(pendingControls) ? pendingControls.size() : 0;
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
        if (optionalControlEntity.isPresent()) {
            return updateExistingControl(optionalControlEntity.get());
        } else {
            return buildNotFoundControlEntity();
        }
    }

    private ControlDto updateExistingControl(ControlEntity controlEntity) {
        if (StatusEnum.PENDING.toString().equals(controlEntity.getStatus())){
            return updatePendingControl(controlEntity);
        } else{
            return mapperUtils.controlEntityToControlDto(controlEntity);
        }
    }

    public ControlDto updatePendingControl(ControlEntity controlEntity) {
        final RequestService requestService = this.getRequestService(controlEntity.getRequestType());
        List<RequestEntity> controlEntityRequests = controlEntity.getRequests();
        if (requestService.allRequestsContainsData(controlEntityRequests)){
            requestService.setDataFromRequests(controlEntity);
            controlEntity.setStatus(StatusEnum.COMPLETE.name());
            return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
        } else {
            return handleExistingControlWithoutData(controlEntity);
        }
    }

    private ControlDto handleExistingControlWithoutData(ControlEntity controlEntity) {
        if (getRequestService(controlEntity.getRequestType()).allRequestsAreInErrorStatus(controlEntity.getRequests())){
            controlEntity.setStatus(StatusEnum.ERROR.name());
            controlEntity.setError(buildErrorEntity(DATA_NOT_FOUND.name(), "Error data not found."));
        } else {
            Instant createdDate = controlEntity.getCreatedDate().toInstant(ZoneOffset.UTC);
            Duration duration = Duration.between(createdDate, Instant.now());
            long seconds = duration.getSeconds();
            if (seconds > timeoutValue){
                controlEntity.setStatus(StatusEnum.TIMEOUT.name());
                return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
            }
        }
        return mapperUtils.controlEntityToControlDto(controlEntity);
    }

    private ControlDto buildNotFoundControlEntity() {
        return mapperUtils.controlEntityToControlDto(ControlEntity.builder()
                .status(StatusEnum.ERROR.name())
                .error(buildErrorEntity(ErrorCodesEnum.UUID_NOT_FOUND.name(), "Error requestUuid not found.")).build());
    }

    private static ErrorEntity buildErrorEntity(String errorCode, String errorDescription) {
        return ErrorEntity.builder()
                .errorCode(errorCode)
                .errorDescription(errorDescription).build();
    }

    public void setError(final ControlDto controlDto, final ErrorDto errorDto) {
        controlDto.setStatus(StatusEnum.ERROR.name());
        controlDto.setError(errorDto);
        this.save(controlDto);
    }

    public void setError(final ControlEntity controlEntity, final ErrorEntity errorEntity) {
        controlEntity.setStatus(StatusEnum.ERROR.name());
        controlEntity.setError(errorEntity);
        controlRepository.save(controlEntity);
    }

    public void setEftiData(final ControlDto controlDto, final byte[] data) {
        controlDto.setStatus(StatusEnum.COMPLETE.name());
        controlDto.setEftiData(data);
        this.save(controlDto);
    }

    private ControlDto save(final ControlDto controlDto) {
        return mapperUtils.controlEntityToControlDto(
                controlRepository.save(mapperUtils. controlDtoToControEntity(controlDto)));
    }

    private Optional<ErrorDto> validateControl(final ValidableDto validable) {
        final Validator validator;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        final Set<ConstraintViolation<ValidableDto>> violations = validator.validate(validable);

        if(violations.isEmpty()) {
            return Optional.empty();
        }

        //we manage only one error by control
        final ConstraintViolation<ValidableDto> constraintViolation = violations.iterator().next();

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

    private RequestService getRequestService(final String requestType) {
        return  requestServiceFactory.getRequestServiceByRequestType(requestType);
    }
}
