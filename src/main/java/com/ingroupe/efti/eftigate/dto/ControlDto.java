package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.eftigate.entity.ErrorEntity;
import com.ingroupe.efti.eftigate.utils.RequestTypeEnum;
import com.ingroupe.efti.eftigate.utils.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlDto {

    private int id;
    private String eftiDataUuid;
    private String requestUuid;
    private String requestType;
    private String status;
    private String eftiPlatformUrl;
    private String eftiGateUrl;
    private String subsetEuRequested;
    private String subsetMsRequested;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private byte[] eftiData;
    private Object transportMetaData;
    private String fromGateUrl;
    private List<RequestDto> requests;
    private AuthorityDto authority;
    private ErrorDto error;

    public ControlDto(UilDto uilDto) {
        String requestUuid = UUID.randomUUID().toString();
        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);

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

    public boolean isError() {
        return StatusEnum.ERROR.name().equals(status);
    }
}
