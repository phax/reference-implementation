package com.ingroupe.efti.edeliveryapconnector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetrieveMessageDto {
    private String messageId;
    private String action;
    private String contentType;
    private MessageBodyDto messageBodyDto;
}
