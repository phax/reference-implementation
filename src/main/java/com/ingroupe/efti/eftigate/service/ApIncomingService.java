package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ApIncomingService {

    private final NotificationService notificationService;
    private final RequestService requestService;
    private final GateProperties gateProperties;

    public void manageIncomingNotification(final ReceivedNotificationDto receivedNotificationDto) {
        final ApConfigDto apConfigDto = ApConfigDto.builder()
                .username(gateProperties.getAp().getUsername())
                .password(gateProperties.getAp().getPassword())
                .url(gateProperties.getAp().getUrl())
                .build();

        notificationService.consume(apConfigDto, receivedNotificationDto).ifPresent(requestService::updateWithResponse);
    }
}
