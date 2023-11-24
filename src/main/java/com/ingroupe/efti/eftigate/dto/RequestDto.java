package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.utils.RequestStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequestDto {

    @NotNull
    private int controlId;

    @NotNull
    private String status;

    private String edeliveryMessageId;

    @NotNull
    private Integer retry;

    private Object reponseData;

    private LocalDateTime lastRetryDate;

    @NotNull
    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    @NotNull
    private String gateUrlDest;

    public RequestDto(ControlEntity controlEntity) {
        LocalDateTime localDateTime = LocalDateTime.now();

        this.controlId = controlEntity.getId();
        this.status = RequestStatusEnum.RECEIVED.toString();
        this.retry = 0;
        this.createdDate = localDateTime;
        this.gateUrlDest = controlEntity.getEftiGateUrl();
    }
}
