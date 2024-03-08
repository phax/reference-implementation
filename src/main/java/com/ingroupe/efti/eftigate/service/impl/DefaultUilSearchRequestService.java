package com.ingroupe.efti.eftigate.service.impl;

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
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.exception.TechnicalException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.ControlService;
import com.ingroupe.efti.eftigate.service.UilSearchRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class DefaultUilSearchRequestService implements UilSearchRequestService {

    private final RequestRepository requestRepository;
    private final RequestSendingService requestSendingService;
    private final GateProperties gateProperties;
    private final MapperUtils mapperUtils;
    private final ObjectMapper objectMapper;
    @Lazy
    private final ControlService controlService;

    @Value("${batch.retry.time}")
    private final List<Integer> listTime;

    @Override
    public RequestDto createAndSendRequest(final ControlDto controlDto) {
        RequestDto requestDto = new RequestDto(controlDto);
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        final RequestDto result = this.save(requestDto);
        this.sendRequest(result, true);
        return result;
    }

    @Override
    public boolean allRequestsContainsData(List<RequestEntity> controlEntityRequests) {
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> Objects.nonNull(requestEntity.getReponseData()));
    }

    @Override
    public void setDataFromRequests(final ControlEntity controlEntity) {
        controlEntity.setEftiData(controlEntity.getRequests().stream()
                .map(RequestEntity::getReponseData).toList().stream()
                .collect(ByteArrayOutputStream::new, (byteArrayOutputStream, bytes) -> byteArrayOutputStream.write(bytes, 0, bytes.length), (arrayOutputStream, byteArrayOutputStream) -> {})
                .toByteArray());
    }

    @Override
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

    @Override
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

        final RequestEntity requestEntity = this.findByRequestUuidOrThrow(messageBody.getRequestUuid());
        if (requestEntity != null){
            if (messageBody.getStatus().equals(StatusEnum.COMPLETE.name())) {
                requestEntity.setReponseData(messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
                //this.controlService.setEftiData(requestDto.getControl(), messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
            } else {
                errorReceived(requestEntity, messageBody.getErrorDescription());
            }
            this.updateStatus(requestEntity, RequestStatusEnum.RECEIVED);
        }

    }
    private void updateStatus(RequestEntity requestEntity, RequestStatusEnum status) {
        requestEntity.setStatus(status.name());
        requestEntity.setLastModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
        requestRepository.save(requestEntity);
    }

    private void errorReceived(final RequestEntity requestEntity, final String errorDescription) {
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

    private void manageSendFailure(final NotificationDto notificationDto) {
        final RequestDto requestDto = this.findByMessageIdOrThrow(notificationDto.getMessageId());
        this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
    }

    private RequestEntity findByRequestUuidOrThrow(String requestId) {
        return Optional.ofNullable(this.requestRepository.findByControlRequestUuid(requestId))
                .orElseThrow(() -> new RequestNotFoundException("couldn't find request for requestUuid: " + requestId));
    }

    private RequestDto findByMessageIdOrThrow(final String messageId) {
        final RequestEntity requestEntity =
                Optional.ofNullable(this.requestRepository.findByEdeliveryMessageId(messageId))
                        .orElseThrow(() -> new RequestNotFoundException("couldn't find request for messageId: " + messageId));
        return mapperUtils.requestToRequestDto(requestEntity);
    }

    public RequestDto updateStatus(final RequestDto requestDto, final RequestStatusEnum status) {
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
