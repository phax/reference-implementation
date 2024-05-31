package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.MetadataResponseDto;
import com.ingroupe.efti.commons.dto.MetadataResultDto;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.IdentifiersMessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.service.RequestUpdaterService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.mapper.SerializeUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.ControlService;
import com.ingroupe.efti.eftigate.service.RabbitSenderService;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.RECEIVED;
import static com.ingroupe.efti.eftigate.constant.EftiGateConstants.IDENTIFIERS_ACTIONS;
import static com.ingroupe.efti.eftigate.constant.EftiGateConstants.IDENTIFIERS_TYPES;
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
        final MetadataResults metadataResults = buildMetadataResult(metadataDtoList);
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
        if (!StatusEnum.ERROR.equals(existingControl.getStatus())) {
            existingControl.setStatus(getControlStatus(existingControl));
        }
        getControlService().save(existingControl);
    }

    private StatusEnum getControlStatus(final ControlEntity existingControl) {
        final StatusEnum currentControlStatus = existingControl.getStatus();
        final List<RequestEntity> requests = existingControl.getRequests();
        if (requests.stream().allMatch(requestEntity -> RequestStatusEnum.SUCCESS == requestEntity.getStatus())) {
            return StatusEnum.COMPLETE;
        } else if (shouldSetTimeout(requests)) {
            return StatusEnum.TIMEOUT;
        } else if (requests.stream().anyMatch(requestEntity -> RequestStatusEnum.ERROR == requestEntity.getStatus())) {
            return StatusEnum.ERROR;
        }
        return currentControlStatus;
    }

    private static boolean shouldSetTimeout(final List<RequestEntity> requests) {
        return requests.stream().anyMatch(requestEntity -> RequestStatusEnum.TIMEOUT == requestEntity.getStatus())
                && requests.stream().noneMatch(requestEntity -> RequestStatusEnum.ERROR == requestEntity.getStatus());
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
    public boolean supports(final EDeliveryAction eDeliveryAction) {
        return IDENTIFIERS_ACTIONS.contains(eDeliveryAction);
    }

    @Override
    public void receiveGateRequest(final NotificationDto notificationDto) {
        throw new UnsupportedOperationException("Forward Operations not supported for Identifiers");
    }

    public void updateControlMetadata(final ControlDto control, final List<MetadataDto> metadataDtoList) {
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
