package com.ingroupe.platform.platformgatesimulator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ApRequestDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.RetrieveMessageDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.platform.platformgatesimulator.config.GateProperties;
import com.ingroupe.platform.platformgatesimulator.dto.BodyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApIncomingService {

    public static final String PATH_CAD = "cda/";
    private final RequestSendingService requestSendingService;

    private final NotificationService notificationService;
    private final GateProperties gateProperties;
    private final ReaderService readerService;

    private final ObjectMapper objectMapper;

    @Value("${gate.receiver}")
    private String receiver;

    @Value("${gate.sender}")
    private String sender;

    public void manageIncomingNotification(final ReceivedNotificationDto receivedNotificationDto) throws IOException {
        final ApConfigDto apConfigDto = ApConfigDto.builder()
                .username(gateProperties.getAp().getUsername())
                .password(gateProperties.getAp().getPassword())
                .url(gateProperties.getAp().getUrl())
                .build();

        Optional<NotificationDto<?>> notificationDto = notificationService.consume(apConfigDto, receivedNotificationDto);
        if (notificationDto.isEmpty()) {
            return;
        }
        RetrieveMessageDto messageBodyDto = (RetrieveMessageDto) notificationDto.get().getContent();
        String eftidataUuid = messageBodyDto.getMessageBodyDto().getEftidataUuid();
        String data = readerService.readFromFile(PATH_CAD + eftidataUuid);
        ApRequestDto apRequestDto = ApRequestDto.builder()
                .requestId(1L).body(buildBody(data, eftidataUuid))
                .apConfig(apConfigDto)
                .receiver(receiver)
                .sender(sender)
                .build();
        try {
            requestSendingService.sendRequest(apRequestDto);
        } catch (SendRequestException e) {
            log.error("SendRequestException received : ", e);
        }
    }

    private String buildBody(String eftiData, String requestUuid) throws JsonProcessingException {
        final BodyDto requestBodyDto = BodyDto.builder()
                .requestUuid(requestUuid)
                .eFTIData(eftiData)
                .status("COMPLETE")
                .build();
        return objectMapper.writeValueAsString(requestBodyDto);
    }
}

