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
    private String eftiDataUuid;

    @NotNull
    private String requestUuid;

    @NotNull
    private String requestType;

    @NotNull
    private String status;

    @NotNull
    private String eftiPlatformUrl;

    @NotNull
    private String eftiGateUrl;

    @NotNull
    private String subsetEuRequested;

    @NotNull
    private String subsetMsRequested;

    @NotNull
    private LocalDateTime createdDate;

    @NotNull
    private LocalDateTime lastModifiedDate;

    private byte[] eftiData;

    private Object transportMetaData;

    private String fromGateUrl;


    public ControlDto(UilDto uilDto) {
        String requestUuid = UUID.randomUUID().toString();
        LocalDateTime localDateTime = LocalDateTime.now();

        this.setEftiDataUuid(uilDto.getUuid());
        this.setEftiGateUrl(uilDto.getGate());
        this.setEftiPlatformUrl(uilDto.getPlatform());
        this.setRequestUuid(requestUuid);
        this.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH.toString());
        this.setStatus(StatusEnum.PENDING.toString());
        this.setSubsetEuRequested("oki");
        this.setSubsetMsRequested("oki");
        this.setCreatedDate(localDateTime);
        this.setLastModifiedDate(localDateTime);
    }
}
