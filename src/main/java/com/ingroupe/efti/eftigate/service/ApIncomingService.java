package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.exception.TechnicalException;
import com.ingroupe.efti.eftigate.mapper.SerializeUtils;
import com.ingroupe.efti.eftigate.service.request.MetadataRequestService;
import com.ingroupe.efti.eftigate.service.request.UilRequestService;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ApIncomingService {

    private final NotificationService notificationService;
    private final UilRequestService uilRequestService;
    private final MetadataRequestService metadataRequestService;
    private final MetadataService metadataService;
    private final GateProperties gateProperties;
    private final SerializeUtils serializeUtils;

    public void manageIncomingNotification(final ReceivedNotificationDto receivedNotificationDto) {
         notificationService.consume(createApConfig(), receivedNotificationDto).ifPresent(this::rootResponse);
    }

    private void rootResponse(final NotificationDto notificationDto) {
        final EDeliveryAction action = EDeliveryAction.getFromValue(notificationDto.getContent().getAction());
        if(action == null) {
            log.error("unknown edelivery action {}", notificationDto.getContent().getAction());
            throw new TechnicalException("unknown edelivery action " + notificationDto.getContent().getAction());
        }
        switch (action) {
            case GET_UIL -> uilRequestService.updateWithResponse(notificationDto);
            case FORWARD_UIL -> uilRequestService.receiveGateRequest(notificationDto);
            case UPLOAD_METADATA -> metadataService.createOrUpdate(parseBodyToMetadata(notificationDto.getContent()));
            case GET_IDENTIFIERS -> metadataRequestService.updateWithResponse(notificationDto);
            default -> log.warn("unmanaged notification type {}", notificationDto.getContent().getAction());
        }
    }

    private MetadataDto parseBodyToMetadata(final NotificationContentDto notificationContent) {
        final String body = serializeUtils.readDataSourceOrThrow(notificationContent.getBody());

        return switch (notificationContent.getContentType()) {
            case MediaType.APPLICATION_JSON_VALUE -> serializeUtils.mapJsonStringToClass(body, MetadataDto.class);
            case MediaType.TEXT_XML_VALUE -> serializeUtils.mapXmlStringToClass(body, MetadataDto.class);
            default -> throw new RetrieveMessageException("unknown content type: " + notificationContent.getContentType());
        };
    }

    private ApConfigDto createApConfig() {
        return ApConfigDto.builder()
                .username(gateProperties.getAp().getUsername())
                .password(gateProperties.getAp().getPassword())
                .url(gateProperties.getAp().getUrl())
                .build();
    }
}
