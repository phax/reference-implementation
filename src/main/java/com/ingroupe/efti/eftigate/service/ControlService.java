package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.MetadataResponseDto;
import com.ingroupe.efti.commons.dto.MetadataResultDto;
import com.ingroupe.efti.commons.dto.ValidableDto;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.IdentifiersMessageBodyDto;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.constant.EftiGateConstants;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.AmbiguousIdentifierException;
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
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.ingroupe.efti.commons.enums.ErrorCodesEnum.DATA_NOT_FOUND;
import static com.ingroupe.efti.commons.enums.RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH;
import static com.ingroupe.efti.commons.enums.StatusEnum.PENDING;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

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

    public ControlEntity getById(final long id) {
        final Optional<ControlEntity> controlEntity = controlRepository.findById(id);
        return controlEntity.orElse(null);
    }

    @Transactional("controlTransactionManager")
    public RequestUuidDto createUilControl(final UilDto uilDto) {
        log.info("create Uil control for uuid : {}", uilDto.getEFTIDataUuid());
        return createControl(uilDto, ControlDto
                .fromUilControl(uilDto, gateProperties.isCurrentGate(uilDto.getEFTIGateUrl()) ? RequestTypeEnum.LOCAL_UIL_SEARCH : RequestTypeEnum.EXTERNAL_UIL_SEARCH));
    }

    public RequestUuidDto createMetadataControl(final MetadataRequestDto metadataRequestDto) {
        log.info("create metadata control for vehicleId : {}", metadataRequestDto.getVehicleID());
        return createControl(metadataRequestDto, ControlDto.fromLocalMetadataControl(metadataRequestDto, RequestTypeEnum.LOCAL_METADATA_SEARCH));
    }

    public ControlDto createControlFrom(final IdentifiersMessageBodyDto messageBody, final String fromGateUrl, MetadataResults metadataResults) {
        final ControlDto controlDto = ControlDto.fromExternalMetadataControl(messageBody, EXTERNAL_ASK_METADATA_SEARCH, fromGateUrl, gateProperties.getOwner(), metadataResults);
        return this.save(controlDto);
    }

    private <T extends ValidableDto> RequestUuidDto createControl(final T searchDto, final ControlDto controlDto) {
        final Optional<ErrorDto> errorOptional = this.validateControl(searchDto);
        errorOptional.ifPresentOrElse(error -> createErrorControl(controlDto, error), () -> {
            if(searchDto instanceof UilDto) {
                createUilControl(controlDto);
            } else if (searchDto instanceof final MetadataRequestDto metadataRequestDto) {
                createMetadataControl(controlDto, metadataRequestDto);
            }
        });
        return buildResponse(controlDto);
    }

    private void createUilControl(final ControlDto controlDto) {
        final ControlDto saveControl = this.save(controlDto);
        getRequestService(controlDto.getRequestType()).createAndSendRequest(saveControl, !gateProperties.isCurrentGate(controlDto.getEftiGateUrl()) ? controlDto.getEftiGateUrl() : null);
        log.info("Uil control with request uuid '{}' has been register", saveControl.getRequestUuid());
    }

    private static void createErrorControl(final ControlDto controlDto, final ErrorDto error) {
        controlDto.setStatus(StatusEnum.ERROR);
        controlDto.setError(error);
        log.error(error.getErrorDescription() + ", " + error.getErrorCode());
    }

    private void createMetadataControl(final ControlDto controlDto, final MetadataRequestDto metadataRequestDto) {
        final List<String> destinationGatesUrls = eftiGateUrlResolver.resolve(metadataRequestDto);
        controlDto.setRequestType(gateToRequestTypeFunction.apply(destinationGatesUrls));
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
        final ControlDto controlDto = getControlByRequestUuid(requestUuid);
        return buildResponse(controlDto);
    }

    public int updatePendingControls(){
        final List<ControlEntity> pendingControls = controlRepository.findByCriteria(PENDING.name(), timeoutValue);
        CollectionUtils.emptyIfNull(pendingControls).forEach(this::updatePendingControl);
        return CollectionUtils.isNotEmpty(pendingControls) ? pendingControls.size() : 0;
    }

    public ControlDto getControlByRequestUuid(final String requestUuid) {
        log.info("get ControlEntity with uuid : {}", requestUuid);
        final Optional<ControlEntity> optionalControlEntity = getByRequestUuid(requestUuid);
        if (optionalControlEntity.isPresent()) {
            return updateExistingControl(optionalControlEntity.get());
        } else {
            return buildNotFoundControlEntity();
        }
    }

    public Optional<ControlEntity> getByRequestUuid(String requestUuid) {
        return controlRepository.findByRequestUuid(requestUuid);
    }

    public ControlDto updateExistingControl(final ControlEntity controlEntity) {
        if (PENDING == controlEntity.getStatus() && isLocalRequest(controlEntity.getRequestType())) {
            return updatePendingControl(controlEntity);
        } else{
            return mapperUtils.controlEntityToControlDto(controlEntity);
        }
    }

    private boolean isLocalRequest(final RequestTypeEnum requestType) {
        return EftiGateConstants.LOCAL_REQUESTS_TYPES.contains(requestType);
    }

    public ControlDto updatePendingControl(final ControlEntity controlEntity) {
        final RequestService requestService = this.getRequestService(controlEntity.getRequestType());
        final List<RequestEntity> controlEntityRequests = controlEntity.getRequests();
        if (requestService.allRequestsContainsData(controlEntityRequests)){
            requestService.setDataFromRequests(controlEntity);
            controlEntity.setStatus(StatusEnum.COMPLETE);
            return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
        } else {
            return handleExistingControlWithoutData(controlEntity);
        }
    }

    public MetadataResponseDto getMetadataResponse(final String requestUuid) {
        final ControlDto controlDto = getControlByRequestUuid(requestUuid);
        return buildMetadataResponse(controlDto);
    }

    public MetadataResponseDto buildMetadataResponse(final ControlDto controlDto) {
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

    private CountryIndicator getEftiGate(final ControlDto controlDto) {
        //temporaire en attendant de discuter sur sa place
        final MetadataResults metadataResults = controlDto.getMetadataResults();
        if (metadataResults != null && CollectionUtils.isNotEmpty(metadataResults.getMetadataResult())){
            final String countryStart = metadataResults.getMetadataResult().iterator().next().getCountryStart();
            if (StringUtils.isNotBlank(countryStart)){
                return CountryIndicator.valueOf(countryStart);
            }
        }
        return null;
    }

    private List<MetadataResultDto> getMetadataResultDtos(final ControlDto controlDto) {
        final MetadataResults metadataResults = controlDto.getMetadataResults();
        if (metadataResults != null){
            return mapperUtils.metadataResultEntitiesToMetadataResultDtos(metadataResults.getMetadataResult());
        }
        return emptyList();
    }

    private ControlDto handleExistingControlWithoutData(final ControlEntity controlEntity) {
        if (getRequestService(controlEntity.getRequestType()).allRequestsAreInErrorStatus(controlEntity.getRequests())){
            controlEntity.setStatus(StatusEnum.ERROR);
            controlEntity.setError(buildErrorEntity(DATA_NOT_FOUND.name(), "Error data not found."));
        } else {
            final Instant createdDate = controlEntity.getCreatedDate().toInstant(ZoneOffset.UTC);
            final Duration duration = Duration.between(createdDate, Instant.now());
            final long seconds = duration.getSeconds();
            if (seconds > timeoutValue){
                controlEntity.setStatus(StatusEnum.TIMEOUT);
                return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
            }
        }
        return mapperUtils.controlEntityToControlDto(controlEntity);
    }

    private ControlDto buildNotFoundControlEntity() {
        return mapperUtils.controlEntityToControlDto(ControlEntity.builder()
                .status(StatusEnum.ERROR)
                .error(buildErrorEntity(ErrorCodesEnum.UUID_NOT_FOUND.name(), "Error requestUuid not found.")).build());
    }

    private static ErrorEntity buildErrorEntity(final String errorCode, final String errorDescription) {
        return ErrorEntity.builder()
                .errorCode(errorCode)
                .errorDescription(errorDescription).build();
    }

    public void setError(final ControlDto controlDto, final ErrorDto errorDto) {
        controlDto.setStatus(StatusEnum.ERROR);
        controlDto.setError(errorDto);
        this.save(controlDto);
    }

    public void setError(final ControlEntity controlEntity, final ErrorEntity errorEntity) {
        controlEntity.setStatus(StatusEnum.ERROR);
        controlEntity.setError(errorEntity);
        controlRepository.save(controlEntity);
    }

    public void setEftiData(final ControlDto controlDto, final byte[] data) {
        controlDto.setStatus(StatusEnum.COMPLETE);
        controlDto.setEftiData(data);
        this.save(controlDto);
    }

    public ControlDto save(final ControlDto controlDto) {
        return mapperUtils.controlEntityToControlDto(
                controlRepository.save(mapperUtils.controlDtoToControEntity(controlDto)));
    }

    public void save(final ControlEntity controlEntity) {
        controlRepository.save(controlEntity);
    }

    private Optional<ErrorDto> validateControl(final ValidableDto validable) {
        final Validator validator;
        try (final ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        final Set<ConstraintViolation<ValidableDto>> violations = validator.validate(validable);

        if(violations.isEmpty()) {
            return Optional.empty();
        }

        //we manage only one error by control
        final ConstraintViolation<ValidableDto> constraintViolation = violations.iterator().next();

        return Optional.of(ErrorDto.fromErrorCode(ErrorCodesEnum.valueOf(constraintViolation.getMessage())));
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

    private RequestService getRequestService(final RequestTypeEnum requestType) {
        return  requestServiceFactory.getRequestServiceByRequestType(requestType);
    }

    public ControlEntity getControlForCriteria(final String requestUuid, final RequestStatusEnum requestStatus) {
        Preconditions.checkArgument(requestUuid != null, "Request Uuid must not be null");
        final List<ControlEntity> controls = controlRepository.findByCriteria(requestUuid, requestStatus);
        if (CollectionUtils.isNotEmpty(controls)) {
            if (controls.size() > 1) {
                throw new AmbiguousIdentifierException(format("Control with request uuid '%s', and request with status '%s' is not unique, %d controls found!", requestUuid, requestStatus, controls.size()));
            } else {
                return controls.get(0);
            }
        }
        return null;
    }
}
