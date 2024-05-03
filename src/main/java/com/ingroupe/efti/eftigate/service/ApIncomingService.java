package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.eftigate.exception.TechnicalException;
import com.ingroupe.efti.eftigate.mapper.SerializeUtils;
import com.ingroupe.efti.eftigate.service.request.EftiRequestUpdater;
import com.ingroupe.efti.eftigate.service.request.RequestService;
import com.ingroupe.efti.eftigate.service.request.RequestServiceFactory;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ApIncomingService {

    private final NotificationService notificationService;
    private final RequestServiceFactory requestServiceFactory;
    private final EftiRequestUpdater eftiRequestUpdater;
    private final MetadataService metadataService;
    private final SerializeUtils serializeUtils;

    public void manageIncomingNotification(final ReceivedNotificationDto receivedNotificationDto) {
         notificationService.consume(receivedNotificationDto).ifPresent(this::rootResponse);
    }

    private void rootResponse(final NotificationDto notificationDto) {
        if (NotificationType.SEND_SUCCESS.equals(notificationDto.getNotificationType())) {
            eftiRequestUpdater.manageSendSuccess(notificationDto);
            return;
        } else if (NotificationType.SEND_FAILURE.equals(notificationDto.getNotificationType())) {
            eftiRequestUpdater.manageSendFailure(notificationDto);
            return;
        }
        final EDeliveryAction action = getAction(notificationDto);
        switch (action) {
            case GET_UIL, GET_IDENTIFIERS -> getRequestService(action).updateWithResponse(notificationDto);
            case FORWARD_UIL -> getRequestService(action).receiveGateRequest(notificationDto);
            case UPLOAD_METADATA -> metadataService.createOrUpdate(parseBodyToMetadata(notificationDto.getContent()));
            default -> log.warn("unmanaged notification type {}", notificationDto.getContent().getAction());
        }
    }

    private static EDeliveryAction getAction(NotificationDto notificationDto) {
        final EDeliveryAction action = EDeliveryAction.getFromValue(notificationDto.getContent().getAction());
        if(action == null) {
            log.error("unknown edelivery action {}", notificationDto.getContent().getAction());
            throw new TechnicalException("unknown edelivery action " + notificationDto.getContent().getAction());
        }
        return action;
    }

    private MetadataDto parseBodyToMetadata(final NotificationContentDto notificationContent) {
        return serializeUtils.mapXmlStringToClass(notificationContent.getBody(), MetadataDto.class);
    }

    private RequestService getRequestService(final EDeliveryAction eDeliveryAction) {
        return  requestServiceFactory.getRequestServiceByEdeliveryActionType(eDeliveryAction);
    }
}
