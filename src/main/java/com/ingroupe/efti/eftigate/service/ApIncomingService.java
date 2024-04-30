package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.mapper.SerializeUtils;
import com.ingroupe.efti.eftigate.service.request.EftiRequestUpdater;
import com.ingroupe.efti.eftigate.service.request.RequestService;
import com.ingroupe.efti.eftigate.service.request.RequestServiceFactory;
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
    private final RequestServiceFactory requestServiceFactory;
    private final EftiRequestUpdater eftiRequestUpdater;
    private final MetadataService metadataService;
    private final GateProperties gateProperties;
    private final SerializeUtils serializeUtils;

    public void manageIncomingNotification(final ReceivedNotificationDto receivedNotificationDto) {
         notificationService.consume(createApConfig(), receivedNotificationDto).ifPresent(this::rootResponse);
    }

    private void rootResponse(final NotificationDto notificationDto) {

        if (NotificationType.SEND_SUCCESS.equals(notificationDto.getNotificationType())) {
            eftiRequestUpdater.manageSendSuccess(notificationDto);
            return;
        } else if (NotificationType.SEND_FAILURE.equals(notificationDto.getNotificationType())) {
            eftiRequestUpdater.manageSendFailure(notificationDto);
            return;
        }
        final EDeliveryAction action = EDeliveryAction.getFromValue(notificationDto.getContent().getAction());
        RequestService requestService = getRequestService(action);
        switch (action) {
            case GET_UIL, GET_IDENTIFIERS -> requestService.updateWithResponse(notificationDto);
            case FORWARD_UIL -> requestService.receiveGateRequest(notificationDto);
            case UPLOAD_METADATA -> metadataService.createOrUpdate(parseBodyToMetadata(notificationDto.getContent()));
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

    private RequestService getRequestService(final EDeliveryAction eDeliveryAction) {
        return  requestServiceFactory.getRequestServiceByEdeliveryActionType(eDeliveryAction);
    }
}
