package com.ingroupe.efti.edeliveryapconnector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedNotificationDto {

    public static final String RECEIVE_SUCCESS = "receiveSuccess";
    public static final String SENT_SUCCESS = "sendSuccess";
    public static final String SENT_FAILURE = "sendFailure";
    public static final String MESSAGE_ID = "messageID";

    @JsonProperty("Body")
    Map<String, Map<String, String>> body;

    public boolean isReceiveSuccess() {
        return body.containsKey(RECEIVE_SUCCESS);
    }

    public boolean isSentSuccess() {
        return body.containsKey(SENT_SUCCESS);
    }

    public boolean isSendFailure() {
        return body.containsKey(SENT_FAILURE);
    }

    public Optional<String> getMessageId() {
        if(isReceiveSuccess()) {
            return Optional.of(this.body.get(RECEIVE_SUCCESS).get(MESSAGE_ID));
        } else if (isSentSuccess()) {
            return Optional.of(this.body.get(SENT_SUCCESS).get(MESSAGE_ID));
        } else if (isSendFailure()) {
            return Optional.of(this.body.get(SENT_FAILURE).get(MESSAGE_ID));
        }
        return Optional.empty();
    }
}
