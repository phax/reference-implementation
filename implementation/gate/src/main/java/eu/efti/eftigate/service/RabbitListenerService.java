package eu.efti.eftigate.service;

import eu.efti.commons.dto.MetadataResponseDto;
import eu.efti.commons.enums.EDeliveryAction;
import eu.efti.commons.enums.RequestStatusEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.commons.enums.StatusEnum;
import eu.efti.edeliveryapconnector.dto.ApConfigDto;
import eu.efti.edeliveryapconnector.dto.ApRequestDto;
import eu.efti.edeliveryapconnector.dto.MessageBodyDto;
import eu.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import eu.efti.edeliveryapconnector.exception.SendRequestException;
import eu.efti.edeliveryapconnector.service.RequestSendingService;
import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.ControlDto;
import eu.efti.eftigate.dto.RequestDto;
import eu.efti.eftigate.dto.requestbody.MetadataRequestBodyDto;
import eu.efti.eftigate.dto.requestbody.RequestBodyDto;
import eu.efti.eftigate.exception.TechnicalException;
import eu.efti.eftigate.mapper.SerializeUtils;
import eu.efti.eftigate.service.request.RequestService;
import eu.efti.eftigate.service.request.RequestServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.function.Function;

import static eu.efti.commons.enums.RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH;
import static eu.efti.eftigate.constant.EftiGateConstants.IDENTIFIERS_TYPES;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class RabbitListenerService {

    @Lazy
    private final ControlService controlService;
    private final GateProperties gateProperties;
    private final SerializeUtils serializeUtils;
    private final RequestSendingService requestSendingService;
    private final RequestServiceFactory requestServiceFactory;
    private final ApIncomingService apIncomingService;
    private final Function<RequestDto, EDeliveryAction> requestToEDeliveryActionFunction;

    @RabbitListener(queues = "${spring.rabbitmq.queues.eftiReceiveMessageQueue:efti.receive-messages.q}")
    public void listenReceiveMessage(final String message) {
        log.info("Receive message from Domibus : {}", message);
        apIncomingService.manageIncomingNotification(
                serializeUtils.mapJsonStringToClass(message, ReceivedNotificationDto.class));
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.messageReceiveDeadLetterQueue:messageReceiveDeadLetterQueue}")
    public void listenMessageReceiveDeadQueue(final String message) {
        log.error("Receive message from dead queue : {}", message);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.eftiSendMessageQueue:efti.send-messages.q}")
    public void listenSendMessage(final String message) {
        log.info("receive message from rabbimq queue");
        trySendDomibus(serializeUtils.mapJsonStringToClass(message, RequestDto.class));
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.messageSendDeadLetterQueue:message-send-dead-letter-queue}")
    public void listenSendMessageDeadLetter(final String message) {
        log.error("Receive message for dead queue");
        final RequestDto requestDto = serializeUtils.mapJsonStringToClass(message, RequestDto.class);
        this.getRequestService(requestDto.getControl().getRequestType()).manageSendError(requestDto);
    }

    private String getBodyForIdentifiersRequest(final RequestDto requestDto) {
        if (EXTERNAL_ASK_METADATA_SEARCH == requestDto.getControl().getRequestType()) { //remote sending response
            final MetadataResponseDto metadataResponseDto = controlService.buildMetadataResponse(requestDto.getControl());
            return serializeUtils.mapObjectToXmlString(metadataResponseDto);
        } else { //local sending request
            final MetadataRequestBodyDto metadataRequestBodyDto = MetadataRequestBodyDto.fromControl(requestDto.getControl());
            return serializeUtils.mapObjectToXmlString(metadataRequestBodyDto);
        }
    }

    private String getBodyForUilRequest(final RequestDto requestDto) {
        final ControlDto controlDto = requestDto.getControl();
        if (requestDto.getStatus() == RequestStatusEnum.RESPONSE_IN_PROGRESS) {
            boolean hasData = requestDto.getReponseData() != null;
            boolean hasError = controlDto.getError() != null;

            return serializeUtils.mapObjectToXmlString(MessageBodyDto.builder()
                    .requestUuid(controlDto.getRequestUuid())
                    .eFTIData(hasData ? new String(requestDto.getReponseData()) : null)
                    .status(hasError ? StatusEnum.ERROR.name() : StatusEnum.COMPLETE.name())
                    .errorDescription(hasError ? controlDto.getError().getErrorDescription() : null)
                    .eFTIDataUuid(controlDto.getEftiDataUuid())
                    .build());
        }

        final RequestBodyDto requestBodyDto = RequestBodyDto.builder()
                .eFTIData(requestDto.getReponseData() != null ? new String(requestDto.getReponseData(), StandardCharsets.UTF_8) : null)
                .eFTIPlatformUrl(requestDto.getControl().getEftiPlatformUrl())
                .requestUuid(controlDto.getRequestUuid())
                .eFTIDataUuid(controlDto.getEftiDataUuid())
                .subsetEU(new LinkedList<>())
                .subsetMS(new LinkedList<>())
                .build();
        return serializeUtils.mapObjectToXmlString(requestBodyDto);
    }

    private ApRequestDto buildApRequestDto(final RequestDto requestDto) {
        final String receiver = gateProperties.isCurrentGate(requestDto.getGateUrlDest()) ? requestDto.getControl().getEftiPlatformUrl() : requestDto.getGateUrlDest();
        return ApRequestDto.builder()
                .sender(gateProperties.getOwner())
                .receiver(receiver)
                .body(IDENTIFIERS_TYPES.contains(requestDto.getControl().getRequestType()) ?
                        getBodyForIdentifiersRequest(requestDto) : getBodyForUilRequest(requestDto))
                .apConfig(ApConfigDto.builder()
                        .username(gateProperties.getAp().getUsername())
                        .password(gateProperties.getAp().getPassword())
                        .url(gateProperties.getAp().getUrl())
                        .build())
                .build();
    }

    private void trySendDomibus(final RequestDto requestDto) {
        try {
            final EDeliveryAction eDeliveryAction = requestToEDeliveryActionFunction.apply(requestDto);
            final String result = this.requestSendingService.sendRequest(buildApRequestDto(requestDto), eDeliveryAction);
            requestDto.setEdeliveryMessageId(result);
            this.getRequestService(requestDto.getControl().getRequestType()).updateStatus(requestDto, RequestStatusEnum.IN_PROGRESS);
        } catch (final SendRequestException e) {
            log.error("error while sending request" + e);
            throw new TechnicalException("Error when try to send message to domibus", e);
        }
    }

    private RequestService getRequestService(final RequestTypeEnum requestType) {
        return  requestServiceFactory.getRequestServiceByRequestType(requestType);
    }
}
