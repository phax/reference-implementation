package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ApRequestDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.RetrieveMessageDto;
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
import com.ingroupe.efti.eftigate.utils.ErrorCodesEnum;
import com.ingroupe.efti.eftigate.utils.RequestStatusEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Service
@AllArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestSendingService requestSendingService;
    private final GateProperties gateProperties;
    private final MapperUtils mapperUtils;
    private final ObjectMapper objectMapper;

    @Value("${batch.retry.time}")
    private List<Integer> listTime;

    public RequestDto createAndSendRequest(final ControlDto controlDto) {
        RequestDto requestDto = new RequestDto(controlDto);
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        final RequestDto result = this.save(requestDto);
        this.sendRequest(result);
        return result;
    }

    public void sendRetryRequest(final RequestDto requestDto) {
        final ApRequestDto apRequestDto;
        try {
            apRequestDto = buildApRequestDto(requestDto);
        } catch (TechnicalException e) {
            log.error("error while building request", e);
            requestDto.setError(ErrorDto.builder().errorCode(ErrorCodesEnum.REQUEST_BUILDING.name()).build());
            this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
            return;
        }
        trySendDomibus(requestDto, apRequestDto);
    }

    public void sendRequest(final RequestDto requestDto) {
        final ApRequestDto apRequestDto;
        try {
            apRequestDto = buildApRequestDto(requestDto);
        } catch (TechnicalException e) {
            log.error("error while building request", e);
            requestDto.setError(ErrorDto.builder().errorCode(ErrorCodesEnum.REQUEST_BUILDING.name()).build());
            this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
            return;
        }

        //see https://stackoverflow.com/questions/49113207/completablefuture-forkjoinpool-set-class-loader/59444016#59444016
        final ForkJoinPool myCommonPool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                new MyForkJoinWorkerThreadFactory()
                , null, false);

        CompletableFuture.runAsync(() -> {
            trySendDomibus(requestDto, apRequestDto);
        }, myCommonPool);
    }

    private void trySendDomibus(RequestDto requestDto, ApRequestDto apRequestDto) {
        try {
            final String result = this.requestSendingService.sendRequest(apRequestDto);
            requestDto.setEdeliveryMessageId(result);
            this.updateStatus(requestDto, RequestStatusEnum.IN_PROGRESS);
        } catch (SendRequestException e) {
            log.error("error while sending request");
            requestDto.setError(ErrorDto.builder()
                    .errorCode(ErrorCodesEnum.AP_SUBMISSION_ERROR.name())
                    .errorDescription("Error while sending request to AP").build());
            requestDto.setNextRetryDate(setNextRetryDate(requestDto));
            requestDto.setRetry(requestDto.getRetry() + 1);
            this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
        }
    }

    private LocalDateTime setNextRetryDate(RequestDto requestDto) {
        LocalDateTime nextRetryDate = LocalDateTime.now(ZoneOffset.UTC);
        try {
            if (listTime.get(requestDto.getRetry()) != null) {
                nextRetryDate = nextRetryDate.plusSeconds(listTime.get(requestDto.getRetry()));
            }
        } catch (IndexOutOfBoundsException e) {
            log.info("Request with id {} have reach the maximal number of retry", requestDto.getId());
            return null;
        }
        return nextRetryDate;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void updateWithResponse(final NotificationDto notificationDto) {
        switch (notificationDto.getNotificationType()) {
            case RECEIVED -> manageMessageReceive(notificationDto);
            case SEND_FAILURE -> manageSendFailure(notificationDto);
            default -> log.warn("unknow notification {} ", notificationDto.getNotificationType());
        }
    }

    private void manageMessageReceive(final NotificationDto<RetrieveMessageDto> notificationDto) {
        final RetrieveMessageDto retrieveMessageDto = notificationDto.getContent();
        final RequestDto requestDto = this.findByRequestUuidOrThrow(retrieveMessageDto.getMessageBodyDto().getRequestUuid());

        requestDto.getControl().setStatus(StatusEnum.COMPLETE.name());
        requestDto.getControl().setEftiData(retrieveMessageDto.getMessageBodyDto().getEFTIData().toString().getBytes());

        this.updateStatus(requestDto, RequestStatusEnum.RECEIVED);
    }

    private void manageSendFailure(final NotificationDto<?> notificationDto) {
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
        if (requestDto.getRetry() > listTime.size()) {
            requestDto.setStatus(RequestStatusEnum.ERROR.toString());
            requestDto.getControl().setStatus(StatusEnum.ERROR.toString());
        }
        return this.save(requestDto);
    }

    private RequestDto save(final RequestDto requestDto) {
        return mapperUtils.requestToRequestDto(requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto)));
    }

    private ApRequestDto buildApRequestDto(final RequestDto requestDto) throws TechnicalException {
        return ApRequestDto.builder()
                .sender(gateProperties.getOwner())
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
