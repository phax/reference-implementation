package com.ingroupe.efti.eftigate.service.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.service.RequestUpdaterService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.mapper.SerializeUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.ControlService;
import com.ingroupe.efti.eftigate.service.RabbitSenderService;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.ERROR;

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
    private final RequestUpdaterService requestUpdaterService;
    private final SerializeUtils serializeUtils;

    @Value("${spring.rabbitmq.queues.eftiSendMessageExchange:efti.send-message.exchange}")
    private String eftiSendMessageExchange;
    @Value("${spring.rabbitmq.queues.eftiKeySendMessage:EFTI}")
    private String eftiKeySendMessage;

    public abstract boolean allRequestsContainsData(List<RequestEntity> controlEntityRequests);

    public abstract void setDataFromRequests(ControlEntity controlEntity);

    public abstract void manageMessageReceive(final NotificationDto notificationDto);

    public abstract boolean supports(final RequestTypeEnum requestTypeEnum);

    public abstract boolean supports(final EDeliveryAction eDeliveryAction);

    public abstract void receiveGateRequest(final NotificationDto notificationDto);

    public void createAndSendRequest(final ControlDto controlDto, final String destinationUrl){
        final RequestDto requestDto = new RequestDto(controlDto);
        requestDto.setGateUrlDest(StringUtils.isNotBlank(destinationUrl) ? destinationUrl : controlDto.getEftiPlatformUrl());
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        final RequestDto result = this.save(requestDto);
        this.sendRequest(result);
    }

    public RequestDto save(final RequestDto requestDto) {
        return mapperUtils.requestToRequestDto(requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto)));
    }

    protected void sendRequest(final RequestDto requestDto) {
        try {
            rabbitSenderService.sendMessageToRabbit(eftiSendMessageExchange, eftiKeySendMessage, requestDto);
        } catch (final JsonProcessingException e) {
            log.error("Error when try to parse object to json/string", e);
        }
    }

    public boolean allRequestsAreInErrorStatus(final List<RequestEntity> controlEntityRequests){
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> ERROR == requestEntity.getStatus());
    }

    public void updateWithResponse(final NotificationDto notificationDto) {
        if (Objects.requireNonNull(notificationDto.getNotificationType()) == NotificationType.RECEIVED) {
            manageMessageReceive(notificationDto);
        } else {
            log.warn("unknown notification {} ", notificationDto.getNotificationType());
        }
    }

    protected String getRequestUuid(final String bodyJsonString) {
        final JsonObject parsed = JsonParser.parseString(bodyJsonString).getAsJsonObject();
        return parsed.get("requestUuid").getAsString();
    }

    public void updateStatus(final RequestDto requestDto, final RequestStatusEnum status, final NotificationDto notificationDto) {
        this.updateStatus(requestDto, status);
        try {
            requestUpdaterService.setMarkedAsDownload(createApConfig(), notificationDto.getMessageId());
        } catch (final MalformedURLException e) {
            log.error("Error while try to set mark as download", e);
        }
    }

    public void updateStatus(final RequestDto requestDto, final RequestStatusEnum status) {
        requestDto.setStatus(status);
        this.save(requestDto);
    }

    public void manageSendError(final RequestDto requestDto) {
        final ErrorDto errorDto = ErrorDto.fromErrorCode(ErrorCodesEnum.AP_SUBMISSION_ERROR);
        requestDto.setError(errorDto);
        controlService.setError(requestDto.getControl(), errorDto);
        this.updateStatus(requestDto, RequestStatusEnum.ERROR);
    }
    protected void errorReceived(final RequestDto requestDto, final String errorDescription) {
        log.error("Error received, change status of requestId : {}", requestDto.getControl().getRequestUuid());
        final ErrorDto errorDto = ErrorDto.builder()
                .errorDescription(errorDescription)
                .errorCode(ErrorCodesEnum.PLATFORM_ERROR.toString())
                .build();

        final ControlDto controlDto = requestDto.getControl();
        controlDto.setError(errorDto);
        controlDto.setStatus(StatusEnum.ERROR);

        requestDto.setControl(controlDto);
        this.save(requestDto);
        controlService.setError(controlDto, errorDto);
    }

    private ApConfigDto createApConfig() {
        return ApConfigDto.builder()
                .username(gateProperties.getAp().getUsername())
                .password(gateProperties.getAp().getPassword())
                .url(gateProperties.getAp().getUrl())
                .build();
    }
}
