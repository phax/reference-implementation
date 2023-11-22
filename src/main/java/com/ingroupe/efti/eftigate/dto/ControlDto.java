package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.eftigate.utils.RequestTypeEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ControlDto {
    @NotNull
    private String eftidatauuid;

    @NotNull
    private String requestuuid;

    @NotNull
    private String requesttype;

    @NotNull
    private String status;

    @NotNull
    private String eftiplatformurl;

    @NotNull
    private String eftigateurl;

    @NotNull
    private String subseteurequested;

    @NotNull
    private String subsetmsrequested;

    @NotNull
    private LocalDateTime createddate;

    @NotNull
    private LocalDateTime lastmodifieddate;

    private byte[] eftidata;

    private Object transportmetadata;

    private String fromgateurl;


    public ControlDto(UilDto uilDto) {
        String requestUuid = UUID.randomUUID().toString();
        LocalDateTime localDateTime = LocalDateTime.now();

        this.setEftidatauuid(uilDto.getUuid());
        this.setEftigateurl(uilDto.getGate());
        this.setEftiplatformurl(uilDto.getPlatform());
        this.setRequestuuid(requestUuid);
        this.setRequesttype(RequestTypeEnum.LOCAL_UIL_SEARCH.toString());
        this.setStatus(StatusEnum.PENDING.toString());
        this.setSubseteurequested("oki");
        this.setSubsetmsrequested("oki");
        this.setCreateddate(localDateTime);
        this.setLastmodifieddate(localDateTime);
    }
}
