package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.commons.dto.AuthorityDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.SearchParameter;
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
    private SearchParameter transportMetaData;
    private String fromGateUrl;
    private List<RequestDto> requests;
    private AuthorityDto authority;
    private ErrorDto error;
    private MetadataResults metadataResults;


    public static ControlDto fromUilControl(final UilDto uilDto) {
        final String uuidGenerator = UUID.randomUUID().toString();
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);

        final ControlDto controlDto = new ControlDto();
        controlDto.setEftiDataUuid(uilDto.getEFTIDataUuid());
        controlDto.setEftiGateUrl(uilDto.getEFTIGateUrl());
        controlDto.setEftiPlatformUrl(uilDto.getEFTIPlatformUrl());
        controlDto.setRequestUuid(uuidGenerator);
        controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH.toString());
        controlDto.setStatus(StatusEnum.PENDING.toString());
        controlDto.setSubsetEuRequested("SubsetEuRequested");
        controlDto.setSubsetMsRequested("SubsetMsRequested");
        controlDto.setCreatedDate(localDateTime);
        controlDto.setLastModifiedDate(localDateTime);
        controlDto.setAuthority(uilDto.getAuthority());
        return controlDto;
    }

    public static ControlDto fromMetadataControl(final MetadataRequestDto metadataRequestDto) {
        final String uuidGenerator = UUID.randomUUID().toString();
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);

        final ControlDto controlDto = new ControlDto();
        controlDto.setRequestUuid(uuidGenerator);
        controlDto.setRequestType(RequestTypeEnum.LOCAL_METADATA_SEARCH.toString());
        controlDto.setStatus(StatusEnum.PENDING.toString());
        controlDto.setSubsetEuRequested("SubsetEuRequested");
        controlDto.setSubsetMsRequested("SubsetMsRequested");
        controlDto.setCreatedDate(localDateTime);
        controlDto.setLastModifiedDate(localDateTime);
        controlDto.setAuthority(metadataRequestDto.getAuthority());
        controlDto.setTransportMetaData(SearchParameter.builder()
                .vehicleId(metadataRequestDto.getVehicleID())
                .transportMode(metadataRequestDto.getTransportMode())
                .vehicleCountry(metadataRequestDto.getVehicleCountry())
                .isDangerousGoods(metadataRequestDto.getIsDangerousGoods())
                .build());
        return controlDto;
    }

    public boolean isError() {
        return StatusEnum.ERROR.name().equals(status);
    }
}
