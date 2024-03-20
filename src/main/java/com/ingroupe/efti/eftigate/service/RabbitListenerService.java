package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ApRequestDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.dto.requestbody.RequestBodyDto;
import com.ingroupe.efti.eftigate.exception.TechnicalException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class RabbitListenerService {

    @Lazy
    private final ControlService controlService;
    private final GateProperties gateProperties;
    private ObjectMapper objectMapper;
    private final RequestSendingService requestSendingService;
    private final MapperUtils mapperUtils;
    private final RequestRepository requestRepository;
    private final ApIncomingService apIncomingService;

    @RabbitListener(queues = "${spring.rabbitmq.queues.eftiReceiveMessageQueue:efti.receive-messages.q}")
    public void listenReceiveMessage(final String message) {
        log.info("Receive message from Domibus : {}", message);
        ReceivedNotificationDto receivedNotificationDto = mapReceivedNotificationDto(message);
        apIncomingService.manageIncomingNotification(receivedNotificationDto);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.messageReceiveDeadLetterQueue:messageReceiveDeadLetterQueue}")
    public void listenMessageReceiveDeadQueue(final String message) {
        log.error("Receive message from dead queue : {}", message);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.eftiSendMessageQueue:efti.send-messages.q}")
    public void listenSendMessage(final String message) {
        log.info("receive message from rabbimq queue");
        RequestDto requestDto = mapRequestDto(message);
        final ApRequestDto apRequestDto;
        try {
            apRequestDto = buildApRequestDto(requestDto);
        } catch (TechnicalException e) {
            log.error("error while building request", e);
            this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
            return;
        }
        trySendDomibus(requestDto, apRequestDto);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.messageSendDeadLetterQueue:message-send-dead-letter-queue}")
    public void listenSendMessageDeadLetter(final String message) {
        log.error("Receive message for dead queue");
        RequestDto requestDto = mapRequestDto(message);
        manageSendError(requestDto);
    }

    private ReceivedNotificationDto mapReceivedNotificationDto(final String message) {
        try {
            objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(message, ReceivedNotificationDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error when try to parse message to RequestDto", e);
            throw new TechnicalException("Error when try to map requestDto with message : " + message);
        }
    }

    private RequestDto mapRequestDto(String message) {
        try {
            objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(message, RequestDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error when try to parse message to RequestDto", e);
            throw new TechnicalException("Error when try to map requestDto with message : " + message);
        }
    }

    private String buildBody(final RequestDto requestDto) throws TechnicalException {
        final RequestBodyDto requestBodyDto = RequestBodyDto.builder()
                .requestUuid(requestDto.getControl().getRequestUuid())
                .eFTIDataUuid(requestDto.getControl().getEftiDataUuid())
                .subsetEU(new LinkedList<>())
                .subsetMS(new LinkedList<>())
                .build();
        try {
            return objectMapper.writeValueAsString(requestBodyDto);
        } catch (JsonProcessingException e) {
            throw new TechnicalException("error while building request body", e);
        }
    }

    private ApRequestDto buildApRequestDto(final RequestDto requestDto) throws TechnicalException {
        return ApRequestDto.builder()
                .sender(requestDto.getControl().getEftiGateUrl())
                .receiver(requestDto.getControl().getEftiPlatformUrl())
                .body(buildBody(requestDto))
                .apConfig(ApConfigDto.builder()
                        .username(gateProperties.getAp().getUsername())
                        .password(gateProperties.getAp().getPassword())
                        .url(gateProperties.getAp().getUrl())
                        .build())
                .build();
    }

    private void trySendDomibus(final RequestDto requestDto, final ApRequestDto apRequestDto) {
        try {
            final String result = this.requestSendingService.sendRequest(apRequestDto, EDeliveryAction.GET_UIL);
            requestDto.setEdeliveryMessageId(result);
            this.updateStatus(requestDto, RequestStatusEnum.IN_PROGRESS);
        } catch (SendRequestException e) {
            log.error("error while sending request");
            throw new TechnicalException("Error when try to send message to domibus");
        }
    }

    private void manageSendError(final RequestDto requestDto) {
        final ErrorDto errorDto = ErrorDto.builder()
                .errorCode(ErrorCodesEnum.AP_SUBMISSION_ERROR.name())
                .errorDescription(ErrorCodesEnum.AP_SUBMISSION_ERROR.getMessage()).build();
        requestDto.setError(errorDto);
        controlService.setError(requestDto.getControl(), errorDto);
        this.updateStatus(requestDto, RequestStatusEnum.ERROR);
    }

    private RequestDto updateStatus(final RequestDto requestDto, final RequestStatusEnum status) {
        requestDto.setStatus(status.name());
        requestDto.setLastModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
        return this.save(requestDto);
    }

    private RequestDto save(final RequestDto requestDto) {
        return mapperUtils.requestToRequestDto(requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto)));
    }
}
