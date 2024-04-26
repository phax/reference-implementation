package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.MetadataResponseDto;
import com.ingroupe.efti.commons.dto.MetadataResultDto;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.IdentifiersMessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.MetadataResult;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.RECEIVED;
import static com.ingroupe.efti.eftigate.constant.EftiGateConstants.IDENTIFIERS_TYPES;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
public class MetadataRequestService extends RequestService {

    @Lazy
    private final MetadataService metadataService;
    public MetadataRequestService(RequestRepository requestRepository, MapperUtils mapperUtils, RabbitSenderService rabbitSenderService, ControlService controlService, GateProperties gateProperties, NotificationService notificationService, MetadataService metadataService) {
        super(requestRepository, mapperUtils, rabbitSenderService, controlService, gateProperties, notificationService);
        this.metadataService = metadataService;
    }

    @Override
    public boolean allRequestsContainsData(List<RequestEntity> controlEntityRequests) {
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> Objects.nonNull(requestEntity.getMetadataResults()) &&
                        isNotEmpty(requestEntity.getMetadataResults().getMetadataResult()));
    }

    @Override
    public void setDataFromRequests(ControlEntity controlEntity) {
        List<MetadataResult> metadataResultList = controlEntity.getRequests().stream()
                .flatMap(request -> request.getMetadataResults().getMetadataResult().stream())
                .toList();
        controlEntity.setMetadataResults(new MetadataResults(metadataResultList));
    }

    @Override
    public void manageMessageReceive(NotificationDto notificationDto) {
        String bodyFromNotification = getBodyFromNotification(notificationDto);
        if (StringUtils.isNotBlank(bodyFromNotification)){
            String requestUuid = getRequestUuid(bodyFromNotification);
            ControlEntity existingControl = getControlService().getControlForCriteria(requestUuid, RequestStatusEnum.IN_PROGRESS.name());
            if (existingControl != null) {
                updateExistingControl(bodyFromNotification, existingControl, notificationDto);
            } else {
                handleNewControlRequest(notificationDto, bodyFromNotification);
            }
        }
    }

    private void handleNewControlRequest(NotificationDto notificationDto, String bodyFromNotification) {
        IdentifiersMessageBodyDto requestMessage = getMessageFromNotificationBody(bodyFromNotification, IdentifiersMessageBodyDto.class);
        ControlDto controlDto = getControlService().createControlFrom(requestMessage, notificationDto.getContent().getFromPartyId());
        List<MetadataDto> metadataDtoList = metadataService.search(buildMetadataRequestDtoFrom(requestMessage));
        RequestDto request = createReceivedRequest(controlDto, metadataDtoList);
        sendRequest(request);
    }

    private void updateExistingControl(String bodyFromNotification, ControlEntity existingControl, NotificationDto notificationDto) {
        MetadataResponseDto response = getMessageFromNotificationBody(bodyFromNotification, MetadataResponseDto.class);
        List<MetadataResultDto> metadataResultDtos = response.getMetadata();
        MetadataResults metadataResults = buildMetadataResultFrom(metadataResultDtos);
        updateControlMetadata(existingControl, metadataResults, metadataResultDtos);
        updateControlRequests(existingControl.getRequests(), metadataResults, notificationDto);
        existingControl.setStatus(getControlStatus(existingControl));
        getControlService().save(existingControl);
    }

    private String getControlStatus(ControlEntity existingControl) {
        String currentControlStatus = existingControl.getStatus();
        List<RequestEntity> remoteGatesRequests = existingControl.getRequests().stream()
                .filter(requestEntity -> StringUtils.isNotBlank(requestEntity.getGateUrlDest())).toList();
        if (remoteGatesRequests.stream().allMatch(requestEntity -> RequestStatusEnum.SUCCESS.name().equalsIgnoreCase(requestEntity.getStatus()))){
             return StatusEnum.COMPLETE.name();
         } else if (shouldSetTimeout(remoteGatesRequests)) {
            return StatusEnum.TIMEOUT.name();
        } else if (remoteGatesRequests.stream().anyMatch(requestEntity -> RequestStatusEnum.ERROR.name().equalsIgnoreCase(requestEntity.getStatus()))){
             return StatusEnum.ERROR.name();
         }
         return currentControlStatus;
    }

    private static boolean shouldSetTimeout(List<RequestEntity> remoteGatesRequests) {
        return remoteGatesRequests.stream().anyMatch(requestEntity -> RequestStatusEnum.TIMEOUT.name().equalsIgnoreCase(requestEntity.getStatus()))
                && remoteGatesRequests.stream().noneMatch(requestEntity -> RequestStatusEnum.ERROR.name().equalsIgnoreCase(requestEntity.getStatus()));
    }

    private void updateControlMetadata(ControlEntity existingControl, MetadataResults metadataResults, List<MetadataResultDto> metadataResultDtos) {
        MetadataResults controlMetadataResults = existingControl.getMetadataResults();
        if (controlMetadataResults == null || controlMetadataResults.getMetadataResult().isEmpty()){
            existingControl.setMetadataResults(metadataResults);
        } else {
            ArrayList<MetadataResult> currentMetadata = new ArrayList<>(controlMetadataResults.getMetadataResult());
            List<MetadataResult> responseMetadata = getMapperUtils().metadataResultDtosToMetadataEntities(metadataResultDtos);
            existingControl.setMetadataResults(MetadataResults.builder().metadataResult(ListUtils.union(currentMetadata, responseMetadata)).build());
        }
    }

    private void updateControlRequests(List<RequestEntity> pendingRequests, MetadataResults metadataResults, NotificationDto notificationDto) {
        CollectionUtils.emptyIfNull(pendingRequests).stream()
                .filter(requestEntity -> isRequestWaitingSentNotification(notificationDto, requestEntity))
                .forEach(requestEntity -> {
                    requestEntity.setMetadataResults(metadataResults);
                    requestEntity.setStatus(RequestStatusEnum.SUCCESS.name());
                });
    }

    private static boolean isRequestWaitingSentNotification(NotificationDto notificationDto, RequestEntity requestEntity) {
        return RequestStatusEnum.IN_PROGRESS.name().equalsIgnoreCase(requestEntity.getStatus())
                && requestEntity.getGateUrlDest() != null
                && requestEntity.getGateUrlDest().equalsIgnoreCase(notificationDto.getContent().getFromPartyId());
    }

    public void createRequest(ControlDto savedControl, List<MetadataDto> metadataDtoList){
        if (isNotEmpty(metadataDtoList)){
            this.createRequest(savedControl, RequestStatusEnum.SUCCESS.name(), metadataDtoList);
        }
        else {
            this.createRequest(savedControl, RequestStatusEnum.ERROR.name(), null);
        }
    }

    public RequestDto createRequest(ControlDto controlDto, String status, List<MetadataDto> metadataDtoList) {
        RequestDto requestDto = save(buildRequestDto(controlDto, status, metadataDtoList));
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        return requestDto;
    }

    private RequestDto buildRequestDto(ControlDto controlDto, String status, List<MetadataDto> metadataDtoList) {
        RequestDto requestDto = RequestDto.builder()
                .createdDate(LocalDateTime.now(ZoneOffset.UTC))
                .retry(0)
                .control(controlDto)
                .status(status)
                .build();
        requestDto.setMetadataResults(buildMetadataResult(metadataDtoList));
        requestDto.setGateUrlDest(controlDto.getFromGateUrl());
        return requestDto;
    }

    private RequestDto createReceivedRequest(ControlDto controlDto, List<MetadataDto> metadataDtoList) {
        RequestDto request = createRequest(controlDto, RECEIVED.name(), metadataDtoList);
        ControlDto updatedControl = getControlService().getControlByRequestUuid(controlDto.getRequestUuid());
        if (StatusEnum.COMPLETE.name().equalsIgnoreCase(updatedControl.getStatus())){
            request.setStatus(RequestStatusEnum.SUCCESS.name());
        }
        request.setControl(updatedControl);
        return request;
    }

    private MetadataRequestDto buildMetadataRequestDtoFrom(IdentifiersMessageBodyDto messageBody) {
        return MetadataRequestDto.builder()
                .vehicleID(messageBody.getVehicleID())
                .isDangerousGoods(messageBody.getIsDangerousGoods())
                .transportMode(messageBody.getTransportMode())
                .vehicleCountry(messageBody.getVehicleCountry())
                .build();
    }

    public MetadataResults buildMetadataResult(List<MetadataDto> metadataDtos) {
        List<MetadataResult> metadataResultList = getMapperUtils().metadataDtosToMetadataEntities(metadataDtos);
        return MetadataResults.builder()
                .metadataResult(metadataResultList)
                .build();
    }

    private MetadataResults buildMetadataResultFrom(List<MetadataResultDto> metadataResultDtos) {
        List<MetadataResult> metadataResultList = getMapperUtils().metadataResultDtosToMetadataEntities(metadataResultDtos);
        return MetadataResults.builder()
                .metadataResult(metadataResultList)
                .build();
    }


    @Override
    public boolean supports(String requestTypeEnum) {
        return IDENTIFIERS_TYPES.contains(requestTypeEnum);
    }
}
