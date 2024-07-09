package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.MetadataResponseDto;
import com.ingroupe.efti.commons.dto.MetadataResultDto;
import com.ingroupe.efti.commons.dto.ValidableDto;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.IdentifiersMessageBodyDto;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.constant.EftiGateConstants;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.NotesDto;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.dto.log.LogRequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.exception.AmbiguousIdentifierException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import com.ingroupe.efti.eftigate.service.gate.EftiGateUrlResolver;
import com.ingroupe.efti.eftigate.service.request.RequestService;
import com.ingroupe.efti.eftigate.service.request.RequestServiceFactory;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
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
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.ingroupe.efti.commons.enums.RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH;
import static com.ingroupe.efti.commons.enums.StatusEnum.PENDING;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class ControlService {

    public static final String ERROR_REQUEST_UUID_NOT_FOUND = "Error requestUuid not found.";
    public static final String CA_AAP = "CA_AAP";
    private final ControlRepository controlRepository;
    private final EftiGateUrlResolver eftiGateUrlResolver;
    private final MetadataService metadataService;
    private final MapperUtils mapperUtils;

    private final RequestServiceFactory requestServiceFactory;

    @Value("${efti.control.pending.timeout:60}")
    private Integer timeoutValue;

    private final Function<List<String>, RequestTypeEnum> gateToRequestTypeFunction;

    private final EftiAsyncCallsProcessor eftiAsyncCallsProcessor;

    private final GateProperties gateProperties;

    private final LoggerService loggerService;

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

    public RequestUuidDto createNoteRequestForControl(final NotesDto notesDto) {
        log.info("create Note Request for control with data uuid : {}", notesDto.getEFTIDataUuid());
        final ControlDto savedControl = getControlByRequestUuid(notesDto.getRequestUuid());
        if (savedControl.isError()) {
            return buildResponse(savedControl);
        }
        return createNoteRequestForControl(savedControl, notesDto);
    }

    public ControlDto createControlFrom(final IdentifiersMessageBodyDto messageBody, final String fromGateUrl, final MetadataResults metadataResults) {
        final ControlDto controlDto = ControlDto.fromExternalMetadataControl(messageBody, EXTERNAL_ASK_METADATA_SEARCH, fromGateUrl, gateProperties.getOwner(), metadataResults);
        return this.save(controlDto);
    }

    private <T extends ValidableDto> RequestUuidDto createControl(final T searchDto, final ControlDto controlDto) {
        this.validateControl(searchDto).ifPresentOrElse(
                error -> createErrorControl(controlDto, error, true),
                () -> createControlFromType(searchDto, controlDto));
        return buildResponse(controlDto);
    }

    private RequestUuidDto createNoteRequestForControl(final ControlDto controlDto, final NotesDto notesDto) {
        final Optional<ErrorDto> errorOptional = this.validateControl(notesDto);
        errorOptional.ifPresentOrElse(error -> createErrorControl(controlDto, error,false), () -> {
            controlDto.setNotes(notesDto.getNote());
            getRequestService(RequestTypeEnum.NOTE_SEND).createAndSendRequest(controlDto, !gateProperties.isCurrentGate(notesDto.getEFTIGateUrl()) ? notesDto.getEFTIGateUrl() : null);
            log.info("Note has been registered for control with request uuid '{}'", controlDto.getRequestUuid());
        });
        return buildResponse(controlDto);

    }

    public void createUilControl(final ControlDto controlDto) {
        if(gateProperties.isCurrentGate(controlDto.getEftiGateUrl()) && !checkOnLocalRegistry(controlDto)) {
            createErrorControl(controlDto, ErrorDto.fromErrorCode(ErrorCodesEnum.DATA_NOT_FOUND_ON_REGISTRY), false);
            final ControlDto saveControl = this.save(controlDto);
            //respond with the error
            if(controlDto.isExternalAsk()) {
                getRequestService(controlDto.getRequestType()).createAndSendRequest(saveControl, controlDto.getFromGateUrl(), RequestStatusEnum.ERROR);
            }
        } else {
            final ControlDto saveControl = this.save(controlDto);
            getRequestService(controlDto.getRequestType()).createAndSendRequest(saveControl, null);
            log.info("Uil control with request uuid '{}' has been register", saveControl.getRequestUuid());
        }
    }

    private boolean checkOnLocalRegistry(final ControlDto controlDto) {
        log.info("checking local registry for dataUuid {}", controlDto.getEftiDataUuid());
        return this.metadataService.existByUIL(controlDto.getEftiDataUuid(), controlDto.getEftiGateUrl(), controlDto.getEftiPlatformUrl());
    }

    private static void createErrorControl(final ControlDto controlDto, final ErrorDto error, final boolean resetUuid) {
        controlDto.setStatus(StatusEnum.ERROR);
        controlDto.setError(error);
        if(resetUuid) {
            controlDto.setRequestUuid(null);
        }
        log.error(error.getErrorDescription() + ", " + error.getErrorCode());
    }

    private void createMetadataControl(final ControlDto controlDto, final MetadataRequestDto metadataRequestDto) {
        final List<String> destinationGatesUrls = eftiGateUrlResolver.resolve(metadataRequestDto);

        controlDto.setRequestType(gateToRequestTypeFunction.apply(destinationGatesUrls));
        final ControlDto saveControl = this.save(controlDto);
        CollectionUtils.emptyIfNull(destinationGatesUrls).forEach(destinationUrl -> {
            if (StringUtils.isBlank(destinationUrl)) {
                getRequestService(saveControl.getRequestType()).createRequest(saveControl, RequestStatusEnum.ERROR);
            } else if (destinationUrl.equalsIgnoreCase(gateProperties.getOwner())){
                eftiAsyncCallsProcessor.checkLocalRepoAsync(metadataRequestDto, saveControl);
            } else {
                getRequestService(saveControl.getRequestType()).createAndSendRequest(saveControl, destinationUrl);
            }
        });
        log.info("Metadata control with request uuid '{}' has been register", saveControl.getRequestUuid());
    }

    private <T> void createControlFromType(final T searchDto, final ControlDto controlDto) {
        if(searchDto instanceof UilDto) {
            createUilControl(controlDto);
        } else if (searchDto instanceof final MetadataRequestDto metadataRequestDto) {
            createMetadataControl(controlDto, metadataRequestDto);
        }
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

    public Optional<ControlEntity> getByRequestUuid(final String requestUuid) {
        return controlRepository.findByRequestUuid(requestUuid);
    }

    public ControlDto updateExistingControl(final ControlEntity controlEntity) {
        if (PENDING == controlEntity.getStatus()) {
            return updatePendingControl(controlEntity);
        } else{
            return mapperUtils.controlEntityToControlDto(controlEntity);
        }
    }

    public ControlDto updatePendingControl(final ControlEntity controlEntity) {
        if (hasRequestInProgress(controlEntity)){
            return mapperUtils.controlEntityToControlDto(controlEntity);
        }
        final RequestService<?> requestService = this.getRequestService(controlEntity.getRequestType());
        final boolean allRequestsContainsData =  requestService.allRequestsContainsData(controlEntity.getRequests());
        if (allRequestsContainsData){
            requestService.setDataFromRequests(controlEntity);
            controlEntity.setStatus(StatusEnum.COMPLETE);
            return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
        } else {
            return handleExistingControlWithoutData(controlEntity);
        }
    }

    private boolean hasRequestInProgress(final ControlEntity controlEntity) {
        return getSecondsSinceCreation(controlEntity) < timeoutValue &&
                CollectionUtils.emptyIfNull(controlEntity.getRequests()).stream().anyMatch(request -> EftiGateConstants.IN_PROGRESS_STATUS.contains(request.getStatus()));
    }

    public MetadataResponseDto getMetadataResponse(final String requestUuid) {
        final ControlDto controlDto = getControlByRequestUuid(requestUuid);
        return buildMetadataResponse(controlDto);
    }

    public MetadataResponseDto buildMetadataResponse(final ControlDto controlDto) {
        final MetadataResponseDto result = MetadataResponseDto.builder()
                .requestUuid(controlDto.getRequestUuid())
                .status(controlDto.getStatus())
                .metadata(getMetadataResultDtos(controlDto)).build();
        if(controlDto.isError()  && controlDto.getError() != null){
            result.setRequestUuid(null);
            result.setErrorDescription(controlDto.getError().getErrorDescription());
            result.setErrorCode(controlDto.getError().getErrorCode());
        }
        return result;
    }

    private List<MetadataResultDto> getMetadataResultDtos(final ControlDto controlDto) {
        final MetadataResults metadataResults = controlDto.getMetadataResults();
        if (metadataResults != null){
            return mapperUtils.metadataResultEntitiesToMetadataResultDtos(metadataResults.getMetadataResult());
        }
        return emptyList();
    }

    private ControlDto handleExistingControlWithoutData(final ControlEntity controlEntity) {
        if (hasRequestInError(controlEntity)){
            controlEntity.setStatus(StatusEnum.ERROR);
        } else if (getSecondsSinceCreation(controlEntity) > timeoutValue) {
            controlEntity.setStatus(StatusEnum.TIMEOUT);
        } else if (PENDING.equals(controlEntity.getStatus())) {
            controlEntity.setStatus(StatusEnum.COMPLETE);
        }
        return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
    }

    private boolean hasRequestInError(final ControlEntity controlEntity) {
        return CollectionUtils.emptyIfNull(controlEntity.getRequests())
                .stream()
                .anyMatch(requestEntity -> RequestStatusEnum.ERROR == requestEntity.getStatus());
    }

    private long getSecondsSinceCreation(final ControlEntity controlEntity) {
        final Instant createdDate = controlEntity.getCreatedDate().toInstant(ZoneOffset.UTC);
        final Duration duration = Duration.between(createdDate, Instant.now());
        return duration.getSeconds();
    }

    private ControlDto buildNotFoundControlEntity() {
        return mapperUtils.controlEntityToControlDto(ControlEntity.builder()
                .status(StatusEnum.ERROR)
                .error(buildErrorEntity(ErrorCodesEnum.UUID_NOT_FOUND.name())).build());
    }

    private static ErrorEntity buildErrorEntity(final String errorCode) {
        return ErrorEntity.builder()
                .errorCode(errorCode)
                .errorDescription(ERROR_REQUEST_UUID_NOT_FOUND).build();
    }

    public void setError(final ControlDto controlDto, final ErrorDto errorDto) {
        controlDto.setStatus(StatusEnum.ERROR);
        controlDto.setError(errorDto);
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
        if(controlDto.isError() && controlDto.getError() != null) {
                result.setErrorDescription(controlDto.getError().getErrorDescription());
                result.setErrorCode(controlDto.getError().getErrorCode());
            }
        final LogRequestDto logRequestDto = LogRequestDto.builder()
                .authorityName(controlDto.getAuthority() != null ? controlDto.getAuthority().getName() : null)
                .authorityNationalUniqueIdentifier(controlDto.getAuthority() != null ? controlDto.getAuthority().getNationalUniqueIdentifier() : null)
                .requestId(controlDto.getRequestUuid())
                .officerId("officerId")
                .responseId("responseId")
                .subsetEURequested(controlDto.getSubsetEuRequested())
                .subsetMSRequested(controlDto.getSubsetMsRequested())
                .eFTIDataId("eFTIDataId")
                .messageEndDate("messageEndDate")
                .componentType(CA_AAP)
                .componentId("compenentId")
                .componentCountry(gateProperties.getCountry())
                .requestingComponentType(CA_AAP)
                .requestingComponentId("requestingComponentId")
                .requestingComponentCountry(gateProperties.getCountry())
                .respondingComponentType(CA_AAP)
                .respondingComponentId("respondingComponentId")
                .respondingComponentCountry(gateProperties.getCountry())
                .messageContent(controlDto.getEftiData() != null ? Base64.getEncoder().encodeToString(controlDto.getEftiData()) : null)
                .statusMessage(controlDto.getStatus().name())
                .errorCodeMessage(controlDto.getError() != null ? controlDto.getError().getErrorCode() : null)
                .errorDescriptionMessage(controlDto.getError() != null ? controlDto.getError().getErrorDescription() : null)
                .timeoutComponentType("timeoutComponentType")
                .build();
        loggerService.log(logRequestDto.getLinkedListFields());
        return result;
    }

    private RequestService<?> getRequestService(final RequestTypeEnum requestType) {
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
