package com.ingroupe.efti.edeliveryapconnector.service;

import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationService {

    private final RequestRetrievingService requestRetrievingService;

    public Optional<NotificationDto> consume(final ApConfigDto apConfigDto, final ReceivedNotificationDto receivedNotificationDto) {
        if(receivedNotificationDto.getMessageId().isEmpty()) {
            log.error("no message id found for notification {}", receivedNotificationDto);
            return Optional.empty();
        }
        if (receivedNotificationDto.isReceiveSuccess()) {
            return onMessageReceived(apConfigDto, receivedNotificationDto);
        } else if (receivedNotificationDto.isSendFailure()) {
            return onSendFailure(receivedNotificationDto);
        } else if (receivedNotificationDto.isSentSuccess()) {
            log.info(" sent message {} successfull", receivedNotificationDto.getMessageId().orElse(null));
            return Optional.empty();
        }
        log.info("received a notification of type {}", receivedNotificationDto.getBody().keySet());
        return Optional.empty();
    }

    private Optional<NotificationDto> onSendFailure(ReceivedNotificationDto receivedNotificationDto) {
        log.info(" sent message {} failed", receivedNotificationDto.getMessageId().orElse(null));

        return Optional.of(NotificationDto.builder()
                .notificationType(NotificationType.SEND_FAILURE)
                .messageId(receivedNotificationDto.getMessageId().orElse(null)).build());
    }

    private Optional<NotificationDto> onMessageReceived(final ApConfigDto apConfigDto, final ReceivedNotificationDto receivedNotificationDto) {
        try {
            return Optional.of(NotificationDto.builder()
                    .messageId(receivedNotificationDto.getMessageId().orElse(null))
                    .notificationType(NotificationType.RECEIVED)
                    .content(this.requestRetrievingService.retrieveMessage(apConfigDto, receivedNotificationDto.getMessageId().orElse(null))).build());
        } catch (SendRequestException | RetrieveMessageFault e) {
            log.error("error while retrieving message " + receivedNotificationDto.getMessageId());
            throw new RetrieveMessageException("error while retrieving message " + receivedNotificationDto.getMessageId().orElse(null)
                    + " " + e.getMessage());
        }
    }
}
