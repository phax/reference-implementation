package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private long id;
    private String status;
    private String edeliveryMessageId;
    private Integer retry;
    private byte[] reponseData;
    private LocalDateTime nextRetryDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String gateUrlDest;
    private ControlDto control;
    private ErrorDto error;
    private MetadataResults metadataResults;


    public RequestDto(final ControlDto controlDto) {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        this.status = RequestStatusEnum.RECEIVED.toString();
        this.retry = 0;
        this.createdDate = localDateTime;
        this.gateUrlDest = controlDto.getEftiGateUrl();
        this.control = controlDto;
    }
}
