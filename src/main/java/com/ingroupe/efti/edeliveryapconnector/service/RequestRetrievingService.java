package com.ingroupe.efti.edeliveryapconnector.service;

import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import eu.domibus.plugin.ws.generated.body.RetrieveMessageRequest;
import eu.domibus.plugin.ws.generated.body.RetrieveMessageResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.ws.Holder;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestRetrievingService extends AbstractApService {

    public NotificationContentDto retrieveMessage(final ApConfigDto apConfigDto, final String messageId) throws SendRequestException, RetrieveMessageFault {
        final RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
        retrieveMessageRequest.setMessageID(messageId);
        retrieveMessageRequest.setMarkAsDownloaded("true");

        final Holder<RetrieveMessageResponse> holderResponse = new Holder<>(new RetrieveMessageResponse());
        final Holder<Messaging> holderMessaging = new Holder<>(new Messaging());

        try {
            initApWebService(apConfigDto).retrieveMessage(retrieveMessageRequest, holderResponse, holderMessaging);

            return NotificationContentDto.builder()
                    .body(holderResponse.value.getPayload().get(0).getValue().getDataSource())
                    .contentType(holderMessaging.value.getUserMessage().getPayloadInfo().getPartInfo().get(0).getPartProperties().getProperty().get(0).getValue())
                    .messageId(holderMessaging.value.getUserMessage().getMessageInfo().getMessageId())
                    .action(holderMessaging.value.getUserMessage().getCollaborationInfo().getAction())
                    .build();
        } catch (IOException e) {
            throw new SendRequestException("error while sending retrieve message request", e);
        }
    }
}
