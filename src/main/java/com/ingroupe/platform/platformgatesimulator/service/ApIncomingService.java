package com.ingroupe.platform.platformgatesimulator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ApRequestDto;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.platform.platformgatesimulator.config.GateProperties;
import com.ingroupe.platform.platformgatesimulator.dto.BodyDto;
import com.ingroupe.platform.platformgatesimulator.exception.UuidFileNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import static java.lang.Thread.sleep;

@Service
@AllArgsConstructor
@Slf4j
public class ApIncomingService {

    private final RequestSendingService requestSendingService;

    private final NotificationService notificationService;

    @Autowired
    private final GateProperties gateProperties;
    private final ReaderService readerService;

    private final ObjectMapper objectMapper;

    public void manageIncomingNotification(final ReceivedNotificationDto receivedNotificationDto) throws IOException, UuidFileNotFoundException, InterruptedException {
        int rand = new Random().nextInt(gateProperties.getMaxSleep()-gateProperties.getMinSleep())+gateProperties.getMinSleep();
        sleep(rand);
        final ApConfigDto apConfigDto = ApConfigDto.builder()
                .username(gateProperties.getAp().getUsername())
                .password(gateProperties.getAp().getPassword())
                .url(gateProperties.getAp().getUrl())
                .build();

        Optional<NotificationDto> notificationDto = notificationService.consume(apConfigDto, receivedNotificationDto);
        if (notificationDto.isEmpty()) {
            return;
        }
        RetrieveMessageDto messageBodyDto = (RetrieveMessageDto) notificationDto.get().getContent();
        String eftidataUuid = messageBodyDto.getMessageBodyDto().getEFTIDataUuid();
        if (eftidataUuid.endsWith("1")) {
            return;
        }
        String requestUuid = messageBody.getRequestUuid();
        String data = readerService.readFromFile(gateProperties.getCdaPath() + eftidataUuid);
        if (data == null) {
            sendError(apConfigDto, eftidataUuid, requestUuid, data);
        } else {
            sendSucess(apConfigDto, eftidataUuid, requestUuid, data);
        }
    }

    private void sendError(ApConfigDto apConfigDto, String eftidataUuid, String requestUuid, String data) throws JsonProcessingException {
        ApRequestDto apRequestDto = ApRequestDto.builder()
                .requestId(1L).body(buildBodyError(data, requestUuid, eftidataUuid, "file not found with uuid"))
                .apConfig(apConfigDto)
                .receiver(gateProperties.getGate())
                .sender(gateProperties.getOwner())
                .build();
        try {
            requestSendingService.sendRequest(apRequestDto);
        } catch (SendRequestException e) {
            log.error("SendRequestException received : ", e);
        }
    }

    private void sendSucess(ApConfigDto apConfigDto, String eftidataUuid, String requestUuid, String data) throws JsonProcessingException {
        ApRequestDto apRequestDto = ApRequestDto.builder()
                .requestId(1L).body(buildBody(data, requestUuid, eftidataUuid))
                .apConfig(apConfigDto)
                .receiver(gateProperties.getGate())
                .sender(gateProperties.getOwner())
                .build();
        try {
            requestSendingService.sendRequest(apRequestDto);
        } catch (SendRequestException e) {
            log.error("SendRequestException received : ", e);
        }
    }

    private String buildBody(String eftiData, String requestUuid, String eftidataUuid) throws JsonProcessingException {
        BodyDto requestBodyDto;
             requestBodyDto = BodyDto.builder()
                    .requestUuid(requestUuid)
                    .eFTIData(eftiData)
                    .status("COMPLETE")
                    .eFTIDataUuid(eftidataUuid)
                    .build();
        return objectMapper.writeValueAsString(requestBodyDto);
    }

    private String buildBodyError(String eftiData, String requestUuid, String eftidataUuid, String errorDescription) throws JsonProcessingException {
        BodyDto requestBodyDto;
             requestBodyDto = BodyDto.builder()
                    .requestUuid(requestUuid)
                    .eFTIData(eftiData)
                    .status("ERROR")
                     .errorDescription(errorDescription)
                    .eFTIDataUuid(eftidataUuid)
                    .build();
        return objectMapper.writeValueAsString(requestBodyDto);
    }
}

