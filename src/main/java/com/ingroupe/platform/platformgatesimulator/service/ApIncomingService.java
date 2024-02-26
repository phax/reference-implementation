package com.ingroupe.platform.platformgatesimulator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ApRequestDto;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.platform.platformgatesimulator.config.GateProperties;
import com.ingroupe.platform.platformgatesimulator.dto.BodyDto;
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

    public void uploadMetadata(final MetadataDto metadataDto) throws JsonProcessingException {
        String metadataDtoString = objectMapper.writeValueAsString(metadataDto);
        final ApConfigDto apConfigDto = ApConfigDto.builder()
                .username(gateProperties.getAp().getUsername())
                .password(gateProperties.getAp().getPassword())
                .url(gateProperties.getAp().getUrl())
                .build();
        sendUpload(apConfigDto, metadataDtoString, EDeliveryAction.UPLOAD_METADATA);
    }

    public void manageIncomingNotification(final ReceivedNotificationDto receivedNotificationDto) throws IOException, InterruptedException {
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
        NotificationContentDto notificationContentDto = notificationDto.get().getContent();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MessageBodyDto messageBody;
        try {
            final String body = IOUtils.toString(notificationContentDto.getBody().getInputStream());
            messageBody = mapper.readValue(body, MessageBodyDto.class);

        } catch (final IOException e) {
            throw new RetrieveMessageException("error while sending retrieve message request", e);
        }
        String eftidataUuid = messageBody.getEFTIDataUuid();
        if (eftidataUuid.endsWith("1")) {
            return;
        }
        String requestUuid = messageBody.getRequestUuid();
        String data = readerService.readFromFile(gateProperties.getCdaPath() + eftidataUuid);
        if (data == null) {
            sendError(apConfigDto, eftidataUuid, requestUuid, data, EDeliveryAction.GET_UIL);
        } else {
            sendSucess(apConfigDto, eftidataUuid, requestUuid, data, EDeliveryAction.GET_UIL);
        }
    }

    private void sendError(ApConfigDto apConfigDto, String eftidataUuid, String requestUuid, String data, final EDeliveryAction eDeliveryAction) throws JsonProcessingException {
        ApRequestDto apRequestDto = ApRequestDto.builder()
                .requestId(1L).body(buildBodyError(data, requestUuid, eftidataUuid, "file not found with uuid"))
                .apConfig(apConfigDto)
                .receiver(gateProperties.getGate())
                .sender(gateProperties.getOwner())
                .build();
        try {
            requestSendingService.sendRequest(apRequestDto, eDeliveryAction);
        } catch (SendRequestException e) {
            log.error("SendRequestException received : ", e);
        }
    }

    private void sendUpload(ApConfigDto apConfigDto, String data, final EDeliveryAction eDeliveryAction) throws JsonProcessingException {
        ApRequestDto apRequestDto = ApRequestDto.builder()
                .requestId(1L).body(data)
                .apConfig(apConfigDto)
                .receiver(gateProperties.getGate())
                .sender(gateProperties.getOwner())
                .build();
        try {
            requestSendingService.sendRequest(apRequestDto, eDeliveryAction);
        } catch (SendRequestException e) {
            log.error("SendRequestException received : ", e);
        }
    }

    private void sendSucess(ApConfigDto apConfigDto, String eftidataUuid, String requestUuid, String data, final EDeliveryAction eDeliveryAction) throws JsonProcessingException {
        ApRequestDto apRequestDto = ApRequestDto.builder()
                .requestId(1L).body(buildBody(data, requestUuid, eftidataUuid))
                .apConfig(apConfigDto)
                .receiver(gateProperties.getGate())
                .sender(gateProperties.getOwner())
                .build();
        try {
            requestSendingService.sendRequest(apRequestDto, eDeliveryAction);
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

