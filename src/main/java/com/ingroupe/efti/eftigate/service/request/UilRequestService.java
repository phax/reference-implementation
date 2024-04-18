package com.ingroupe.efti.eftigate.service.request;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.ControlService;
import com.ingroupe.efti.eftigate.service.RabbitSenderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ingroupe.efti.eftigate.constant.EftiGateConstants.UIL_TYPES;

@Slf4j
@Component
public class UilRequestService extends RequestService {
    public UilRequestService(RequestRepository requestRepository, MapperUtils mapperUtils, RabbitSenderService rabbitSenderService, ControlService controlService, GateProperties gateProperties) {
        super(requestRepository, mapperUtils, rabbitSenderService, controlService, gateProperties);
    }

    @Override
    public boolean allRequestsContainsData(List<RequestEntity> controlEntityRequests) {
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> Objects.nonNull(requestEntity.getReponseData()));
    }

    @Override
    public void setDataFromRequests(ControlEntity controlEntity) {
        controlEntity.setEftiData(controlEntity.getRequests().stream()
                .map(RequestEntity::getReponseData).toList().stream()
                .collect(ByteArrayOutputStream::new, (byteArrayOutputStream, bytes) -> byteArrayOutputStream.write(bytes, 0, bytes.length), (arrayOutputStream, byteArrayOutputStream) -> {
                })
                .toByteArray());
    }

    @Override
    public void manageMessageReceive(final NotificationDto notificationDto) {
        MessageBodyDto messageBody = getMessageBodyDtoFromNotificationDto(notificationDto);

        final RequestEntity requestEntity = this.findByRequestUuidOrThrow(messageBody.getRequestUuid());
        if (messageBody.getStatus().equals(StatusEnum.COMPLETE.name())) {
            requestEntity.setReponseData(messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
            this.updateStatus(requestEntity, RequestStatusEnum.SUCCESS);
        } else {
            this.updateStatus(requestEntity, RequestStatusEnum.ERROR);
            errorReceived(requestEntity, messageBody.getErrorDescription());
        }
        responseToOtherGateIfNecessary(requestEntity);

    }

    private void responseToOtherGateIfNecessary(RequestEntity requestEntity) {
        if (requestEntity.getControl().getFromGateUrl() != null &&
                !getGateProperties().isCurrentGate(requestEntity.getControl().getFromGateUrl())) {
            RequestDto requestDto = getMapperUtils().requestToRequestDto(requestEntity);
            requestDto.setGateUrlDest(requestDto.getControl().getFromGateUrl());
            requestDto.getControl().setRequests(null);
            requestDto.getControl().setEftiData(requestDto.getReponseData());
            requestDto.getControl().setStatus(requestDto.getReponseData() != null ? StatusEnum.COMPLETE.name() : StatusEnum.ERROR.name());
            if (!StatusEnum.COMPLETE.name().equals(requestDto.getControl().getStatus())) {
                ErrorDto errorDto = new ErrorDto();
                errorDto.setErrorCode(ErrorCodesEnum.DATA_NOT_FOUND.name());
                errorDto.setErrorDescription(ErrorCodesEnum.DATA_NOT_FOUND.getMessage());
                requestDto.getControl().setError(errorDto);
            }
            getControlService().save(requestDto.getControl());
            this.sendRequest(requestDto);
        }
    }

    private RequestEntity findByRequestUuidOrThrow(String requestId) {
        return Optional.ofNullable(this.getRequestRepository().findByControlRequestUuidAndStatus(requestId, RequestStatusEnum.IN_PROGRESS.name()))
                .orElseThrow(() -> new RequestNotFoundException("couldn't find request for requestUuid: " + requestId));
    }


    public void receiveGateRequest(final NotificationDto notificationDto) {
        MessageBodyDto messageBody = getMessageBodyDtoFromNotificationDto(notificationDto);

        RequestEntity requestEntity = getRequestRepository()
                .findByControlRequestUuidAndStatus(messageBody.getRequestUuid(), RequestStatusEnum.IN_PROGRESS.name());
        manageRequestAndSend(notificationDto, requestEntity, messageBody);
    }

    private void manageRequestAndSend(NotificationDto notificationDto, RequestEntity requestEntity, MessageBodyDto messageBody) {
        if (requestEntity == null) {
            askReception(notificationDto, messageBody);
        } else {
            receptionOfResponse(requestEntity, messageBody);
        }
    }

    private MessageBodyDto getMessageBodyDtoFromNotificationDto(NotificationDto notificationDto) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            final String body = IOUtils.toString(notificationDto.getContent().getBody().getInputStream());
            return mapper.readValue(body, MessageBodyDto.class);
        } catch (final IOException e) {
            throw new RetrieveMessageException("error while sending retrieve message request", e);
        }
    }

    private void receptionOfResponse(RequestEntity requestEntity, MessageBodyDto messageBody) {
        if (messageBody.getEFTIData() != null) {
            requestEntity.setReponseData(messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
            requestEntity.setStatus(RequestStatusEnum.SUCCESS.name());
        } else {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setErrorCode(ErrorCodesEnum.DATA_NOT_FOUND.name());
            errorDto.setErrorDescription(ErrorCodesEnum.DATA_NOT_FOUND.getMessage());
            requestEntity.setError(getMapperUtils().errorDtoToErrorEntity(errorDto));
            requestEntity.setStatus(RequestStatusEnum.ERROR.name());
        }
        getRequestRepository().save(requestEntity);
    }

    private void askReception(NotificationDto notificationDto, MessageBodyDto messageBody) {
        ControlDto controlDto = ControlDto
                .fromGateToGateMessageBodyDto(messageBody, RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH.name(),
                        notificationDto, getGateProperties().getOwner());
        controlDto = getControlService().save(controlDto);
        this.createAndSendRequest(controlDto);
    }

    public RequestDto createAndSendRequest(final ControlDto controlDto) {
        RequestDto requestDto = new RequestDto(controlDto);
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        final RequestDto result = this.save(requestDto);
        this.sendRequest(result);
        return result;
    }

    @Override
    public boolean supports(String requestTypeEnum) {
        return UIL_TYPES.contains(requestTypeEnum);
    }
}
