package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.exception.RequestNotFoundException;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.ControlService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.ingroupe.efti.commons.enums.RequestStatusEnum.SEND_ERROR;
import static com.ingroupe.efti.commons.enums.RequestStatusEnum.SUCCESS;

@Slf4j
@Component
@AllArgsConstructor
public class EftiRequestUpdater {
    private final RequestRepository requestRepository;
    private final ControlService controlService;

    public void manageSendFailure(final NotificationDto notificationDto) {
        this.updateStatus(findRequestByMessageIdOrThrow(notificationDto.getMessageId()), SEND_ERROR);
    }

    public void manageSendSuccess(final NotificationDto notificationDto) {
        RequestEntity externalRequest = this.requestRepository.findByControlRequestTypeInAndEdeliveryMessageId(List.of(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH, RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH), notificationDto.getMessageId());
        if (externalRequest == null) {
            log.info(" sent message {} successfully", notificationDto.getMessageId());
        } else {
            externalRequest.getControl().setStatus(StatusEnum.COMPLETE);
            this.updateStatus(externalRequest, SUCCESS);
        }
    }

    protected RequestEntity findRequestByMessageIdOrThrow(final String messageId) {
        return Optional.ofNullable(this.requestRepository.findByEdeliveryMessageId(messageId))
                .orElseThrow(() -> new RequestNotFoundException("couldn't find request for messageId: " + messageId));
    }

    public void updateStatus(final RequestEntity request, final RequestStatusEnum status) {
        request.setStatus(status);
        controlService.save(request.getControl());
    }
}
