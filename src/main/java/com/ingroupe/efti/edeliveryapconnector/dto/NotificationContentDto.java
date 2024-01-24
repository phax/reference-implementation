package com.ingroupe.efti.edeliveryapconnector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.activation.DataSource;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationContentDto {
    private String messageId;
    private String action;
    private String contentType;
    private DataSource body;
}
