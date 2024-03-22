package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
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

@Slf4j
@Component
public class MetadataRequestService extends RequestService {

    @Lazy
    private final MetadataService metadataService;
    public MetadataRequestService(RequestRepository requestRepository, MapperUtils mapperUtils, RabbitSenderService rabbitSenderService, ControlService controlService, MetadataService metadataService) {
        super(requestRepository, mapperUtils, rabbitSenderService, controlService);
        this.metadataService = metadataService;
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
        MessageBodyDto messageBody = getMessageBodyFromNotification(notificationDto);
        final RequestEntity requestEntity = this.findByRequestUuidOrThrow(messageBody.getRequestUuid());
        if (requestEntity != null){
            if (messageBody.getStatus().equals(StatusEnum.COMPLETE.name())) {
                //A faire next jira
                List<MetadataDto> metadataDtoList = metadataService.search(null);
                if (CollectionUtils.isNotEmpty(metadataDtoList)){
                    requestEntity.setMetadataResults(buildMetadataResult(metadataDtoList));
                }
                //requestEntity.setReponseData(messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
                //this.controlService.setEftiData(requestDto.getControl(), messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
            } else {
                errorReceived(requestEntity, messageBody.getErrorDescription());
            }
            this.updateStatus(requestEntity, RequestStatusEnum.RECEIVED);
        }
    }

    private MetadataResults buildMetadataResult(List<MetadataDto> metadataDtos) {
        List<MetadataResult> metadataResultList = getMapperUtils().metadataDtosToMetadataEntities(metadataDtos);
        return MetadataResults.builder()
                .metadataResult(metadataResultList)
                .build();
    }

    @Override
    public boolean supports(String requestTypeEnum) {
        return IDENTIFIERS_TYPES.contains(requestTypeEnum);
    }
}
