package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.utils.RequestStatusEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequestDto {

    @NotNull
    private int controlid;

    @NotNull
    private String status;

    private String edeliverymessageid;

    @NotNull
    private Integer retry;

    private Object reponsedata;

    private LocalDateTime lastretrydate;

    @NotNull
    private LocalDateTime createddate;

    private LocalDateTime lastmodifieddate;

    @NotNull
    private String gateurldest;

    public RequestDto(ControlEntity controlEntity) {
        LocalDateTime localDateTime = LocalDateTime.now();

        this.controlid = controlEntity.getId();
        this.status = RequestStatusEnum.RECEIVED.toString();
        this.retry = 0;
        this.createddate = localDateTime;
        this.gateurldest = controlEntity.getEftigateurl();
    }
}
