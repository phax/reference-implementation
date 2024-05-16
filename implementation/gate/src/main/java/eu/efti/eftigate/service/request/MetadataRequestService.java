package eu.efti.eftigate.service.request;

import eu.efti.commons.dto.MetadataDto;
import eu.efti.commons.dto.MetadataRequestDto;
import eu.efti.commons.dto.MetadataResponseDto;
import eu.efti.commons.dto.MetadataResultDto;
import eu.efti.commons.enums.EDeliveryAction;
import eu.efti.commons.enums.RequestStatusEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.commons.enums.StatusEnum;
import eu.efti.edeliveryapconnector.dto.IdentifiersMessageBodyDto;
import eu.efti.edeliveryapconnector.dto.NotificationDto;
import eu.efti.edeliveryapconnector.service.RequestUpdaterService;
import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.ControlDto;
import eu.efti.eftigate.dto.RequestDto;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.entity.MetadataResult;
import eu.efti.eftigate.entity.MetadataResults;
import eu.efti.eftigate.entity.RequestEntity;
import eu.efti.eftigate.mapper.MapperUtils;
import eu.efti.eftigate.mapper.SerializeUtils;
import eu.efti.eftigate.repository.RequestRepository;
import eu.efti.eftigate.service.ControlService;
import eu.efti.eftigate.service.RabbitSenderService;
import eu.efti.metadataregistry.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static eu.efti.commons.enums.RequestStatusEnum.RECEIVED;
import static eu.efti.eftigate.constant.EftiGateConstants.IDENTIFIERS_ACTIONS;
import static eu.efti.eftigate.constant.EftiGateConstants.IDENTIFIERS_TYPES;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
public class MetadataRequestService extends RequestService {

    @Lazy
    private final MetadataService metadataService;

    public MetadataRequestService(final RequestRepository requestRepository,
                                  final MapperUtils mapperUtils,
                                  final RabbitSenderService rabbitSenderService,
                                  final ControlService controlService,
                                  final GateProperties gateProperties,
                                  final MetadataService metadataService,
                                  final RequestUpdaterService requestUpdaterService,
                                  final SerializeUtils serializeUtils) {
        super(requestRepository, mapperUtils, rabbitSenderService, controlService, gateProperties, requestUpdaterService, serializeUtils);
        this.metadataService = metadataService;
    }


    @Override
    public boolean allRequestsContainsData(final List<RequestEntity> controlEntityRequests) {
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> Objects.nonNull(requestEntity.getMetadataResults()) &&
                        isNotEmpty(requestEntity.getMetadataResults().getMetadataResult()));
    }

    @Override
    public void setDataFromRequests(final ControlEntity controlEntity) {
        final List<MetadataResult> metadataResultList = controlEntity.getRequests().stream()
                .flatMap(request -> request.getMetadataResults().getMetadataResult().stream())
                .toList();
        controlEntity.setMetadataResults(new MetadataResults(metadataResultList));
    }

    @Override
    public void manageMessageReceive(final NotificationDto notificationDto) {
        final String bodyFromNotification = notificationDto.getContent().getBody();
        if (StringUtils.isNotBlank(bodyFromNotification)){
            final String requestUuid = getRequestUuid(bodyFromNotification);
            final ControlEntity existingControl = getControlService().getControlForCriteria(requestUuid, RequestStatusEnum.IN_PROGRESS);
            if (existingControl != null) {
                updateExistingControl(bodyFromNotification, existingControl, notificationDto);
            } else {
                handleNewControlRequest(notificationDto, bodyFromNotification);
            }
        }
    }

    private void handleNewControlRequest(final NotificationDto notificationDto, final String bodyFromNotification) {
        final IdentifiersMessageBodyDto requestMessage = getSerializeUtils().mapXmlStringToClass(bodyFromNotification, IdentifiersMessageBodyDto.class);
        final List<MetadataDto> metadataDtoList = metadataService.search(buildMetadataRequestDtoFrom(requestMessage));
        MetadataResults metadataResults = buildMetadataResult(metadataDtoList);
        final ControlDto controlDto = getControlService().createControlFrom(requestMessage, notificationDto.getContent().getFromPartyId(), metadataResults);
        final RequestDto request = createReceivedRequest(controlDto, metadataDtoList);
        sendRequest(request);
    }

    private void updateExistingControl(final String bodyFromNotification, final ControlEntity existingControl, final NotificationDto notificationDto) {
        final MetadataResponseDto response = getSerializeUtils().mapXmlStringToClass(bodyFromNotification, MetadataResponseDto.class);
        final List<MetadataResultDto> metadataResultDtos = response.getMetadata();
        final MetadataResults metadataResults = buildMetadataResultFrom(metadataResultDtos);
        updateControlMetadata(existingControl, metadataResults, metadataResultDtos);
        updateControlRequests(existingControl.getRequests(), metadataResults, notificationDto);
        existingControl.setStatus(getControlStatus(existingControl));
        getControlService().save(existingControl);
    }

    private StatusEnum getControlStatus(final ControlEntity existingControl) {
        final StatusEnum currentControlStatus = existingControl.getStatus();
        List<RequestEntity> remoteGatesRequests = existingControl.getRequests().stream()
                .filter(requestEntity -> StringUtils.isNotBlank(requestEntity.getGateUrlDest())).toList();
        if (remoteGatesRequests.stream().allMatch(requestEntity -> RequestStatusEnum.SUCCESS == requestEntity.getStatus())) {
            return StatusEnum.COMPLETE;
        } else if (shouldSetTimeout(remoteGatesRequests)) {
            return StatusEnum.TIMEOUT;
        } else if (remoteGatesRequests.stream().anyMatch(requestEntity -> RequestStatusEnum.ERROR == requestEntity.getStatus())) {
            return StatusEnum.ERROR;
        }
        return currentControlStatus;
    }

    private static boolean shouldSetTimeout(List<RequestEntity> remoteGatesRequests) {
        return remoteGatesRequests.stream().anyMatch(requestEntity -> RequestStatusEnum.TIMEOUT == requestEntity.getStatus())
                && remoteGatesRequests.stream().noneMatch(requestEntity -> RequestStatusEnum.ERROR == requestEntity.getStatus());
    }

    private void updateControlMetadata(final ControlEntity existingControl, final MetadataResults metadataResults, final List<MetadataResultDto> metadataResultDtos) {
        final MetadataResults controlMetadataResults = existingControl.getMetadataResults();
        if (controlMetadataResults == null || controlMetadataResults.getMetadataResult().isEmpty()) {
            existingControl.setMetadataResults(metadataResults);
        } else {
            final ArrayList<MetadataResult> currentMetadata = new ArrayList<>(controlMetadataResults.getMetadataResult());
            final List<MetadataResult> responseMetadata = getMapperUtils().metadataResultDtosToMetadataEntities(metadataResultDtos);
            existingControl.setMetadataResults(MetadataResults.builder().metadataResult(ListUtils.union(currentMetadata, responseMetadata)).build());
        }
    }

    private void updateControlRequests(final List<RequestEntity> pendingRequests, final MetadataResults metadataResults, final NotificationDto notificationDto) {
        CollectionUtils.emptyIfNull(pendingRequests).stream()
                .filter(requestEntity -> isRequestWaitingSentNotification(notificationDto, requestEntity))
                .forEach(requestEntity -> {
                    requestEntity.setMetadataResults(metadataResults);
                    requestEntity.setStatus(RequestStatusEnum.SUCCESS);
                });
    }

    private static boolean isRequestWaitingSentNotification(final NotificationDto notificationDto, final RequestEntity requestEntity) {
        return RequestStatusEnum.IN_PROGRESS == requestEntity.getStatus()
                && requestEntity.getGateUrlDest() != null
                && requestEntity.getGateUrlDest().equalsIgnoreCase(notificationDto.getContent().getFromPartyId());
    }

    public RequestDto createRequest(final ControlDto savedControl, final List<MetadataDto> metadataDtoList) {
        if (isNotEmpty(metadataDtoList)) {
            return this.createRequest(savedControl, RequestStatusEnum.SUCCESS, metadataDtoList);
        } else {
            return this.createRequest(savedControl, RequestStatusEnum.ERROR, null);
        }
    }

    public RequestDto createRequest(final ControlDto controlDto, final RequestStatusEnum status, final List<MetadataDto> metadataDtoList) {
        final RequestDto requestDto = save(buildRequestDto(controlDto, status, metadataDtoList));
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        return requestDto;
    }

    private RequestDto buildRequestDto(final ControlDto controlDto, final RequestStatusEnum status, final List<MetadataDto> metadataDtoList) {
        return RequestDto.builder()
                .retry(0)
                .control(controlDto)
                .status(status)
                .metadataResults(buildMetadataResult(metadataDtoList))
                .gateUrlDest(controlDto.getFromGateUrl())
                .build();
    }

    private RequestDto createReceivedRequest(final ControlDto controlDto, final List<MetadataDto> metadataDtoList) {
        final RequestDto request = createRequest(controlDto, RECEIVED, metadataDtoList);
        final ControlDto updatedControl = getControlService().getControlByRequestUuid(controlDto.getRequestUuid());
        if (StatusEnum.COMPLETE == updatedControl.getStatus()) {
            request.setStatus(RequestStatusEnum.RESPONSE_IN_PROGRESS);
        }
        request.setControl(updatedControl);
        return request;
    }

    private MetadataRequestDto buildMetadataRequestDtoFrom(final IdentifiersMessageBodyDto messageBody) {
        return MetadataRequestDto.builder()
                .vehicleID(messageBody.getVehicleID())
                .isDangerousGoods(messageBody.getIsDangerousGoods())
                .transportMode(messageBody.getTransportMode())
                .vehicleCountry(messageBody.getVehicleCountry())
                .build();
    }

    public MetadataResults buildMetadataResult(final List<MetadataDto> metadataDtos) {
        final List<MetadataResult> metadataResultList = getMapperUtils().metadataDtosToMetadataEntities(metadataDtos);
        return MetadataResults.builder()
                .metadataResult(metadataResultList)
                .build();
    }

    private MetadataResults buildMetadataResultFrom(final List<MetadataResultDto> metadataResultDtos) {
        final List<MetadataResult> metadataResultList = getMapperUtils().metadataResultDtosToMetadataEntities(metadataResultDtos);
        return MetadataResults.builder()
                .metadataResult(metadataResultList)
                .build();
    }


    @Override
    public boolean supports(final RequestTypeEnum requestTypeEnum) {
        return IDENTIFIERS_TYPES.contains(requestTypeEnum);
    }

    @Override
    public boolean supports(EDeliveryAction eDeliveryAction) {
        return IDENTIFIERS_ACTIONS.contains(eDeliveryAction);
    }

    @Override
    public void receiveGateRequest(NotificationDto notificationDto) {
        throw new UnsupportedOperationException("Forward Operations not supported for Identifiers");
    }

    public void updateControlMetadata(ControlDto control, List<MetadataDto> metadataDtoList) {
        getControlService().getByRequestUuid(control.getRequestUuid()).ifPresent(controlEntity -> {
            if (controlEntity.getMetadataResults() == null || controlEntity.getMetadataResults().getMetadataResult().isEmpty())
            {
                controlEntity.setMetadataResults(buildMetadataResult(metadataDtoList));
            } else {
                final ArrayList<MetadataResult> existingMetadata = new ArrayList<>(controlEntity.getMetadataResults().getMetadataResult());
                final List<MetadataResult> responseMetadata = getMapperUtils().metadataDtosToMetadataEntities(metadataDtoList);
                controlEntity.setMetadataResults(MetadataResults.builder().metadataResult(ListUtils.union(existingMetadata, responseMetadata)).build());
            }
            getControlService().save(controlEntity);
        });
    }
}
