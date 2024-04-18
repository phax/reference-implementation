package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import com.ingroupe.efti.eftigate.service.request.MetadataRequestService;
import com.ingroupe.efti.eftigate.service.request.UilRequestService;
import com.ingroupe.efti.metadataregistry.service.MetadataService;
import com.sun.istack.ByteArrayDataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
@Slf4j
public class ApIncomingService {

    private final NotificationService notificationService;
    private final UilRequestService uilRequestService;
    private final MetadataRequestService metadataRequestService;
    private final MetadataService metadataService;
    private final GateProperties gateProperties;

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
        try {
            final ByteArrayDataSource source = (ByteArrayDataSource) notificationContent.getBody();
            final String body = IOUtils.toString(source.getInputStream(), StandardCharsets.UTF_8);

            return switch (notificationContent.getContentType()) {
                case MediaType.APPLICATION_JSON_VALUE -> mapFromJson(body);
                case MediaType.TEXT_XML_VALUE -> mapFromXml(body);
                default -> throw new RetrieveMessageException("unknown content type: " + notificationContent.getContentType());
            };
        } catch (IOException e) {
            throw new RetrieveMessageException("error while parsing body", e);
        }
    }

    private static MetadataDto mapFromXml(final String body) throws JsonProcessingException {
        final XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return xmlMapper.readValue(body, MetadataDto.class);
    }

    private static MetadataDto mapFromJson(final String body) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper.readValue(body, MetadataDto.class);
    }

    private ApConfigDto createApConfig() {
        return ApConfigDto.builder()
                .username(gateProperties.getAp().getUsername())
                .password(gateProperties.getAp().getPassword())
                .url(gateProperties.getAp().getUrl())
                .build();
    }
}
