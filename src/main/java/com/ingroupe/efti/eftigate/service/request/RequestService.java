package com.ingroupe.efti.eftigate.service.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.ControlService;
import com.ingroupe.efti.eftigate.service.RabbitSenderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.ERROR;
import static com.ingroupe.efti.commons.enums.RequestStatusEnum.SEND_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Getter
public abstract class RequestService {
    private final RequestRepository requestRepository;
    private final MapperUtils mapperUtils;
    private final RabbitSenderService rabbitSenderService;
    @Lazy
    private final ControlService controlService;
    private final GateProperties gateProperties;


    @Value("${spring.rabbitmq.queues.eftiSendMessageExchange:efti.send-message.exchange}")
    private String eftiSendMessageExchange;
    @Value("${spring.rabbitmq.queues.eftiKeySendMessage:EFTI}")
    private String eftiKeySendMessage;


    public abstract boolean allRequestsContainsData(List<RequestEntity> controlEntityRequests);

    public abstract void setDataFromRequests(ControlEntity controlEntity);

    public abstract void manageMessageReceive(final NotificationDto notificationDto);

    public abstract boolean supports(final String requestTypeEnum);

    public void createAndSendRequest(ControlDto controlDto, String destinationUrl){
        RequestDto requestDto = new RequestDto(controlDto);
        requestDto.setGateUrlDest(StringUtils.isNotBlank(destinationUrl) ? destinationUrl : controlDto.getEftiPlatformUrl());
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        final RequestDto result = this.save(requestDto);
        this.sendRequest(result);
    }

    protected RequestDto save(final RequestDto requestDto) {
        return mapperUtils.requestToRequestDto(requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto)));
    }

    protected void sendRequest(final RequestDto requestDto) {
        try {
            rabbitSenderService.sendMessageToRabbit(eftiSendMessageExchange, eftiKeySendMessage, requestDto);
        } catch (JsonProcessingException e) {
            log.error("Error when try to parse object to json/string", e);
        }
    }

    public boolean allRequestsAreInErrorStatus(List<RequestEntity> controlEntityRequests){
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> ERROR.name().equalsIgnoreCase(requestEntity.getStatus()));
    }

    public void updateWithResponse(final NotificationDto notificationDto) {
        switch (notificationDto.getNotificationType()) {
            case RECEIVED -> manageMessageReceive(notificationDto);
            case SEND_FAILURE -> manageSendFailure(notificationDto);
            default -> log.warn("unknown notification {} ", notificationDto.getNotificationType());
        }
    }

    protected void manageSendFailure(final NotificationDto notificationDto) {
        this.updateStatus(findRequestByMessageIdOrThrow(notificationDto.getMessageId()), SEND_ERROR);
    }

    protected <T> T getMessageBodyFromNotification(NotificationDto notificationDto, Class<T> targetClass) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
         try {
            final String body = IOUtils.toString(notificationDto.getContent().getBody().getInputStream());
            return mapper.readValue(body, targetClass);
        } catch (final IOException e) {
            throw new RetrieveMessageException("error while sending retrieve message request", e);
        }
    }

    protected RequestEntity findRequestByMessageIdOrThrow(final String messageId) {
        return Optional.ofNullable(this.requestRepository.findByEdeliveryMessageId(messageId))
                        .orElseThrow(() -> new RequestNotFoundException("couldn't find request for messageId: " + messageId));
    }

    protected void updateStatus(RequestEntity requestEntity, RequestStatusEnum status) {
        requestEntity.setStatus(status.name());
        requestEntity.setLastModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
        requestRepository.save(requestEntity);
    }

    protected void errorReceived(final RequestEntity requestEntity, final String errorDescription) {
        log.error("Error received, change status of requestId : {}", requestEntity.getControl().getRequestUuid());
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        final ErrorEntity errorEntity = ErrorEntity.builder()
                .errorDescription(errorDescription)
                .errorCode(ErrorCodesEnum.PLATFORM_ERROR.toString())
                .build();
        final ControlEntity controlEntity = requestEntity.getControl();
        controlEntity.setLastModifiedDate(localDateTime);
        controlEntity.setLastModifiedDate(localDateTime);
        controlEntity.setError(errorEntity);
        controlEntity.setStatus(StatusEnum.ERROR.name());
        controlEntity.setError(errorEntity);
        requestEntity.setControl(controlEntity);
        requestRepository.save(requestEntity);
        controlService.setError(controlEntity, errorEntity);
    }
}
