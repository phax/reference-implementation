package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.edeliveryapconnector.dto.IdentifiersMessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.RECEIVED;
import static com.ingroupe.efti.commons.enums.RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH;
import static com.ingroupe.efti.eftigate.constant.EftiGateConstants.IDENTIFIERS_TYPES;

@Slf4j
@Component
public class MetadataRequestService extends RequestService {

    @Lazy
    private final MetadataService metadataService;
    private final MetadataLocalRequestService metadataLocalRequestService;
    public MetadataRequestService(RequestRepository requestRepository, MapperUtils mapperUtils, RabbitSenderService rabbitSenderService, ControlService controlService, GateProperties gateProperties, MetadataService metadataService, MetadataLocalRequestService metadataLocalRequestService, NotificationService notificationService) {
        super(requestRepository, mapperUtils, rabbitSenderService, controlService, gateProperties, notificationService);
        this.metadataService = metadataService;
        this.metadataLocalRequestService = metadataLocalRequestService;
    }


    @Override
    public boolean allRequestsContainsData(List<RequestEntity> controlEntityRequests) {
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> Objects.nonNull(requestEntity.getMetadataResults()));
    }

    @Override
    public void setDataFromRequests(ControlEntity controlEntity) {
        List<MetadataResult> metadataResultList = controlEntity.getRequests().stream()
                .flatMap(s -> s.getMetadataResults().getMetadataResult().stream())
                .toList();
        controlEntity.setMetadataResults(new MetadataResults(metadataResultList));
    }

    @Override
    public void manageMessageReceive(NotificationDto notificationDto) {
        IdentifiersMessageBodyDto messageBody = getMessageBodyFromNotification(notificationDto, IdentifiersMessageBodyDto.class);
        ControlDto controlDto = ControlDto.fromExternalMetadataControl(messageBody, EXTERNAL_ASK_METADATA_SEARCH.name(), notificationDto, getGateProperties().getOwner());
        controlDto = getControlService().save(controlDto);
        MetadataRequestDto metadataRequestDto = getMetadataRequestDto(messageBody);
        List<MetadataDto> metadataDtoList = metadataService.search(metadataRequestDto);
        metadataLocalRequestService.createRequest(controlDto, RECEIVED.name(), metadataDtoList );
        //respond to caller (another ticket)
    }

    private MetadataRequestDto getMetadataRequestDto(IdentifiersMessageBodyDto messageBody) {
        return MetadataRequestDto.builder()
                .vehicleID(messageBody.getVehicleID())
                .isDangerousGoods(messageBody.getIsDangerousGoods())
                .transportMode(messageBody.getTransportMode())
                .vehicleCountry(messageBody.getVehicleCountry())
                .build();
    }

    @Override
    public boolean supports(String requestTypeEnum) {
        return IDENTIFIERS_TYPES.contains(requestTypeEnum);
    }
}
