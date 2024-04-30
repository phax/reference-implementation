package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.mapper.SerializeUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.ControlService;
import com.ingroupe.efti.eftigate.service.RabbitSenderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ingroupe.efti.eftigate.constant.EftiGateConstants.UIL_ACTIONS;
import static com.ingroupe.efti.eftigate.constant.EftiGateConstants.UIL_TYPES;

@Slf4j
@Component
public class UilRequestService extends RequestService {

    public UilRequestService(final RequestRepository requestRepository,
                             final MapperUtils mapperUtils,
                             final RabbitSenderService rabbitSenderService,
                             final ControlService controlService,
                             final GateProperties gateProperties,
                             final NotificationService notificationService,
                             final SerializeUtils serializeUtils) {
        super(requestRepository, mapperUtils, rabbitSenderService, controlService, gateProperties, notificationService, serializeUtils);
    }

    @Override
    public boolean allRequestsContainsData(final List<RequestEntity> controlEntityRequests) {
        return CollectionUtils.emptyIfNull(controlEntityRequests).stream()
                .allMatch(requestEntity -> Objects.nonNull(requestEntity.getReponseData()));
    }

    @Override
    public void setDataFromRequests(final ControlEntity controlEntity) {
        controlEntity.setEftiData(controlEntity.getRequests().stream()
                .map(RequestEntity::getReponseData).toList().stream()
                .collect(ByteArrayOutputStream::new, (byteArrayOutputStream, bytes) -> byteArrayOutputStream.write(bytes, 0, bytes.length), (arrayOutputStream, byteArrayOutputStream) -> {
                })
                .toByteArray());
    }

    @Override
    public void manageMessageReceive(final NotificationDto notificationDto) {
        final MessageBodyDto messageBody =
                getSerializeUtils().mapDataSourceToClass(notificationDto.getContent().getBody(),MessageBodyDto.class);

        final RequestDto requestDto = this.findByRequestUuidOrThrow(messageBody.getRequestUuid());
        if (messageBody.getStatus().equals(StatusEnum.COMPLETE.name())) {
            requestDto.setReponseData(messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
            this.updateStatus(requestDto, RequestStatusEnum.SUCCESS, notificationDto);
        } else {
            this.updateStatus(requestDto, RequestStatusEnum.ERROR, notificationDto);
            errorReceived(requestDto, messageBody.getErrorDescription());
        }

        responseToOtherGateIfNecessary(requestDto);
    }

    private void responseToOtherGateIfNecessary(final RequestDto requestDto) {
        if (requestDto.getControl().getFromGateUrl() != null &&
                !getGateProperties().isCurrentGate(requestDto.getControl().getFromGateUrl())) {
            requestDto.setGateUrlDest(requestDto.getControl().getFromGateUrl());
            //todo suspect
            requestDto.getControl().setRequests(null);
            requestDto.getControl().setEftiData(requestDto.getReponseData());
            requestDto.getControl().setStatus(requestDto.getReponseData() != null ? StatusEnum.COMPLETE : StatusEnum.ERROR);
            if (!StatusEnum.COMPLETE.equals(requestDto.getControl().getStatus())) {
                requestDto.getControl().setError(ErrorDto.fromErrorCode(ErrorCodesEnum.DATA_NOT_FOUND));
            }
            getControlService().save(requestDto.getControl());
            this.sendRequest(requestDto);
        }
    }

    private RequestDto findByRequestUuidOrThrow(final String requestId) {
        return getMapperUtils().requestToRequestDto(Optional.ofNullable(
                this.getRequestRepository().findByControlRequestUuidAndStatus(requestId, RequestStatusEnum.IN_PROGRESS))
                .orElseThrow(() -> new RequestNotFoundException("couldn't find request for requestUuid: " + requestId)));
    }

    @Override
    public void receiveGateRequest(final NotificationDto notificationDto) {
        final MessageBodyDto messageBody = getSerializeUtils().mapDataSourceToClass(notificationDto.getContent().getBody(), MessageBodyDto.class);

        final RequestEntity requestEntity = getRequestRepository()
                .findByControlRequestUuidAndStatus(messageBody.getRequestUuid(), RequestStatusEnum.IN_PROGRESS);
        manageRequestAndSend(notificationDto, requestEntity, messageBody);
    }

    private void manageRequestAndSend(final NotificationDto notificationDto, final RequestEntity requestEntity, final MessageBodyDto messageBody) {
        if (requestEntity == null) {
            askReception(notificationDto, messageBody);
        } else {
            receptionOfResponse(requestEntity, messageBody);
        }
    }

    private void receptionOfResponse(final RequestEntity requestEntity, final MessageBodyDto messageBody) {
        if (messageBody.getEFTIData() != null) {
            requestEntity.setReponseData(messageBody.getEFTIData().toString().getBytes(StandardCharsets.UTF_8));
            requestEntity.setStatus(RequestStatusEnum.SUCCESS);
        } else {
            requestEntity.setError(getMapperUtils().errorDtoToErrorEntity(ErrorDto.fromErrorCode(ErrorCodesEnum.DATA_NOT_FOUND)));
            requestEntity.setStatus(RequestStatusEnum.ERROR);
        }
        getRequestRepository().save(requestEntity);
    }

    private void askReception(final NotificationDto notificationDto, final MessageBodyDto messageBody) {
        final ControlDto controlDto = ControlDto
                .fromGateToGateMessageBodyDto(messageBody, RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH,
                        notificationDto, getGateProperties().getOwner());
        this.createAndSendRequest(getControlService().save(controlDto));
    }

    public void createAndSendRequest(final ControlDto controlDto) {
        final RequestDto requestDto = new RequestDto(controlDto);
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        final RequestDto result = this.save(requestDto);
        this.sendRequest(result);
    }

    @Override
    public boolean supports(final RequestTypeEnum requestTypeEnum) {
        return UIL_TYPES.contains(requestTypeEnum);
    }

    @Override
    public boolean supports(EDeliveryAction eDeliveryAction) {
        return UIL_ACTIONS.contains(eDeliveryAction);
    }
}
