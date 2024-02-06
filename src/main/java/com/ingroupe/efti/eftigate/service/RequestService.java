package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ApRequestDto;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.config.MyForkJoinWorkerThreadFactory;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.dto.requestbody.RequestBodyDto;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.exception.TechnicalException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestSendingService requestSendingService;
    private final GateProperties gateProperties;
    private final MapperUtils mapperUtils;
    private final ObjectMapper objectMapper;
    @Lazy
    private final ControlService controlService;

    @Value("${batch.retry.time}")
    private final List<Integer> listTime;

    public RequestDto createAndSendRequest(final ControlDto controlDto) {
        RequestDto requestDto = new RequestDto(controlDto);
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        final RequestDto result = this.save(requestDto);
        this.sendRequest(result, true);
        return result;
    }

    public void sendRequest(final RequestDto requestDto, final boolean runAsynch) {
        final ApRequestDto apRequestDto;
        try {
            apRequestDto = buildApRequestDto(requestDto);
        } catch (TechnicalException e) {
            log.error("error while building request", e);
            requestDto.setError(ErrorDto.builder().errorCode(ErrorCodesEnum.REQUEST_BUILDING.name()).build());
            this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
            return;
        }

        if(runAsynch) {
            //see https://stackoverflow.com/questions/49113207/completablefuture-forkjoinpool-set-class-loader/59444016#59444016
            final ForkJoinPool myCommonPool = new ForkJoinPool(
                    Runtime.getRuntime().availableProcessors(),
                    new MyForkJoinWorkerThreadFactory()
                    , null, false);

            CompletableFuture.runAsync(() -> trySendDomibus(requestDto, apRequestDto), myCommonPool);
        } else {
            trySendDomibus(requestDto, apRequestDto);
        }
    }

    public void updateWithResponse(final NotificationDto notificationDto) {
        switch (notificationDto.getNotificationType()) {
            case RECEIVED -> manageMessageReceive(notificationDto);
            case SEND_FAILURE -> manageSendFailure(notificationDto);
            default -> log.warn("unknown notification {} ", notificationDto.getNotificationType());
        }
    }

    private void trySendDomibus(final RequestDto requestDto, final ApRequestDto apRequestDto) {
        try {
            final String result = this.requestSendingService.sendRequest(apRequestDto, EDeliveryAction.GET_UIL);
            requestDto.setEdeliveryMessageId(result);
            this.updateStatus(requestDto, RequestStatusEnum.IN_PROGRESS);
        } catch (SendRequestException e) {
            log.error("error while sending request");
            manageSendError(requestDto);
        }
    }

    private void manageSendError(final RequestDto requestDto) {
        final ErrorDto errorDto = ErrorDto.builder()
                .errorCode(ErrorCodesEnum.AP_SUBMISSION_ERROR.name())
                .errorDescription(ErrorCodesEnum.AP_SUBMISSION_ERROR.getMessage()).build();
        requestDto.setError(errorDto);

        final Optional<LocalDateTime> optionalDate = setNextRetryDate(requestDto);
        optionalDate.ifPresentOrElse(requestDto::setNextRetryDate,
                () -> controlService.setError(requestDto.getControl(), errorDto));
        requestDto.setRetry(requestDto.getRetry() + 1);
        this.updateStatus(requestDto, optionalDate.isPresent() ? RequestStatusEnum.SEND_ERROR : RequestStatusEnum.ERROR);
    }

    private Optional<LocalDateTime> setNextRetryDate(final RequestDto requestDto) {
        LocalDateTime nextRetryDate = LocalDateTime.now(ZoneOffset.UTC);
        try {
            if (listTime.get(requestDto.getRetry()) != null) {
                nextRetryDate = nextRetryDate.plusSeconds(listTime.get(requestDto.getRetry()));
            }
        } catch (IndexOutOfBoundsException e) {
            log.info("Request with id {} have reach the maximal number of retry", requestDto.getId());
            return Optional.empty();
        }
        return Optional.of(nextRetryDate);
    }

    private void manageMessageReceive(final NotificationDto notificationDto) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MessageBodyDto messageBody;
        try {
            final String body = IOUtils.toString(notificationDto.getContent().getBody().getInputStream());
            messageBody = mapper.readValue(body, MessageBodyDto.class);

        } catch (final IOException e) {
            throw new RetrieveMessageException("error while sending retrieve message request", e);
        }

        final RequestDto requestDto = this.findByRequestUuidOrThrow(messageBody.getRequestUuid());
        if (messageBody.getStatus().equals(StatusEnum.COMPLETE.name())) {
            this.controlService.setEftiData(requestDto.getControl(), messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
        } else {
            errorReceived(requestDto, messageBody.getErrorDescription());
        }
        this.updateStatus(requestDto, RequestStatusEnum.RECEIVED);
    }

    private void errorReceived(final RequestDto requestDto, final String errorDescription) {
        log.error("Error received, change status of requestId : {}", requestDto.getControl().getRequestUuid());
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        final ErrorDto errorDto = ErrorDto.builder()
                .errorDescription(errorDescription)
                .errorCode(ErrorCodesEnum.PLATFORM_ERROR.toString())
                .build();
        final ControlDto controlDto = requestDto.getControl();
        controlDto.setLastModifiedDate(localDateTime);
        requestDto.setLastModifiedDate(localDateTime);
        requestDto.setError(errorDto);
        controlDto.setStatus(StatusEnum.ERROR.name());
        controlDto.setError(errorDto);
        requestDto.setControl(controlDto);
        this.save(requestDto);
        controlService.setError(controlDto, errorDto);
    }

    private void manageSendFailure(final NotificationDto notificationDto) {
        final RequestDto requestDto = this.findByMessageIdOrThrow(notificationDto.getMessageId());
        this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
    }

    private RequestDto findByRequestUuidOrThrow(final String requestId) {
        final RequestEntity requestEntity =
                Optional.ofNullable(this.requestRepository.findByControlRequestUuid(requestId))
                        .orElseThrow(() -> new RequestNotFoundException("couldn't find request for requestUuid: " + requestId));
        return mapperUtils.requestToRequestDto(requestEntity);
    }

    private RequestDto findByMessageIdOrThrow(final String messageId) {
        final RequestEntity requestEntity =
                Optional.ofNullable(this.requestRepository.findByEdeliveryMessageId(messageId))
                        .orElseThrow(() -> new RequestNotFoundException("couldn't find request for messageId: " + messageId));
        return mapperUtils.requestToRequestDto(requestEntity);
    }

    private RequestDto updateStatus(final RequestDto requestDto, final RequestStatusEnum status) {
        requestDto.setStatus(status.name());
        requestDto.setLastModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
        return this.save(requestDto);
    }

    private RequestDto save(final RequestDto requestDto) {
        return mapperUtils.requestToRequestDto(requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto)));
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
}
