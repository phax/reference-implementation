package com.ingroupe.efti.edeliveryapconnector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto<T> {
    private NotificationType notificationType;
    private String messageId;
    private T content;
}
