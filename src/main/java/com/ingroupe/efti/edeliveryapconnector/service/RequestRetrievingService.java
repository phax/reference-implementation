package com.ingroupe.efti.edeliveryapconnector.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.RetrieveMessageDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.sun.istack.ByteArrayDataSource;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import eu.domibus.plugin.ws.generated.body.RetrieveMessageRequest;
import eu.domibus.plugin.ws.generated.body.RetrieveMessageResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.stereotype.Component;

import javax.xml.ws.Holder;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestRetrievingService extends AbstractApService {

    public RetrieveMessageDto retrieveMessage(final ApConfigDto apConfigDto, final String messageId) throws SendRequestException, RetrieveMessageFault {
        final RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
        retrieveMessageRequest.setMessageID(messageId);
        retrieveMessageRequest.setMarkAsDownloaded("true");

        final Holder<RetrieveMessageResponse> holderResponse = new Holder<>(new RetrieveMessageResponse());
        final Holder<Messaging> holderMessaging = new Holder<>(new Messaging());

        try {
            initApWebService(apConfigDto).retrieveMessage(retrieveMessageRequest, holderResponse, holderMessaging);

            return RetrieveMessageDto.builder()
                    .messageBodyDto(getMessageBody(holderResponse.value))
                    .contentType(holderResponse.value.getBodyload().getContentType())
                    .messageId(holderMessaging.value.getUserMessage().getMessageInfo().getMessageId())
                    .action(holderMessaging.value.getUserMessage().getCollaborationInfo().getAction())
                    .build();
        } catch (IOException e) {
            throw new SendRequestException("error while sending retrieve message request", e);
        }
    }

    private MessageBodyDto getMessageBody(final RetrieveMessageResponse retrieveMessageResponse) throws IOException {
        final ByteArrayDataSource source = (ByteArrayDataSource) retrieveMessageResponse.getPayload().get(0).getValue().getDataSource();
        final String body = IOUtils.toString(source.getInputStream());

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper.readValue(body, MessageBodyDto.class);
    }
}
