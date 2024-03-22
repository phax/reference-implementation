package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
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

@Slf4j
@Component
public class UilRequestService extends RequestService {
    public UilRequestService(RequestRepository requestRepository, MapperUtils mapperUtils, RabbitSenderService rabbitSenderService, ControlService controlService) {
        super(requestRepository, mapperUtils, rabbitSenderService, controlService);
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
                .collect(ByteArrayOutputStream::new, (byteArrayOutputStream, bytes) -> byteArrayOutputStream.write(bytes, 0, bytes.length), (arrayOutputStream, byteArrayOutputStream) -> {})
                .toByteArray());
    }

    @Override
    public void manageMessageReceive(NotificationDto notificationDto) {
        MessageBodyDto messageBody = getMessageBodyFromNotification(notificationDto);
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

    @Override
    public boolean supports(String requestTypeEnum) {
        return UIL_TYPES.contains(requestTypeEnum);
    }
}
