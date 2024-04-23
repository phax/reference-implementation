package com.ingroupe.efti.eftigate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ingroupe.efti.commons.dto.AuthorityDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.IdentifiersMessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.entity.SearchParameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.ingroupe.efti.commons.enums.StatusEnum.PENDING;

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
    @ToString.Exclude
    @JsonIgnore
    private List<RequestDto> requests;
    private AuthorityDto authority;
    private ErrorDto error;
    private MetadataResults metadataResults;

    public static ControlDto fromGateToGateMessageBodyDto(MessageBodyDto messageBodyDto, String requestTypeEnum, NotificationDto notificationDto, String eftiGateUrl) {
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);

        final ControlDto controlDto = new ControlDto();
        controlDto.setEftiDataUuid(messageBodyDto.getEFTIDataUuid());
        controlDto.setEftiGateUrl(eftiGateUrl);
        controlDto.setFromGateUrl(notificationDto.getContent().getFromPartyId());
        controlDto.setEftiPlatformUrl(messageBodyDto.getEFTIPlatformUrl());
        controlDto.setRequestUuid(messageBodyDto.getRequestUuid());
        controlDto.setRequestType(requestTypeEnum);
        controlDto.setStatus(StatusEnum.PENDING.toString());
        controlDto.setSubsetEuRequested("SubsetEuRequested");
        controlDto.setSubsetMsRequested("SubsetMsRequested");
        controlDto.setCreatedDate(localDateTime);
        controlDto.setLastModifiedDate(localDateTime);
        controlDto.setAuthority(null);
        return controlDto;
    }

    public static ControlDto fromUilControl(final UilDto uilDto, RequestTypeEnum requestTypeEnum) {
        final String uuidGenerator = UUID.randomUUID().toString();
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);

        final ControlDto controlDto = new ControlDto();
        controlDto.setEftiDataUuid(uilDto.getEFTIDataUuid());
        controlDto.setEftiGateUrl(uilDto.getEFTIGateUrl());
        controlDto.setEftiPlatformUrl(uilDto.getEFTIPlatformUrl());
        controlDto.setRequestUuid(uuidGenerator);
        controlDto.setRequestType(requestTypeEnum.name());
        controlDto.setStatus(StatusEnum.PENDING.toString());
        controlDto.setSubsetEuRequested("SubsetEuRequested");
        controlDto.setSubsetMsRequested("SubsetMsRequested");
        controlDto.setCreatedDate(localDateTime);
        controlDto.setLastModifiedDate(localDateTime);
        controlDto.setAuthority(uilDto.getAuthority());
        return controlDto;
    }

    public static ControlDto fromLocalMetadataControl(final MetadataRequestDto metadataRequestDto, String requestTypeEnum) {
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        AuthorityDto authorityDto = metadataRequestDto.getAuthority();

        ControlDto controlDto = getControlFrom(requestTypeEnum, localDateTime, authorityDto, UUID.randomUUID().toString());
        controlDto.setTransportMetaData(SearchParameter.builder()
                .vehicleId(metadataRequestDto.getVehicleID())
                .transportMode(metadataRequestDto.getTransportMode())
                .vehicleCountry(metadataRequestDto.getVehicleCountry())
                .isDangerousGoods(metadataRequestDto.getIsDangerousGoods())
                .build());
        return controlDto;
    }

    public static ControlDto fromExternalMetadataControl(IdentifiersMessageBodyDto messageBodyDto, String requestTypeEnum, String fromGateUrl, String eftiGateUrl) {
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);

        ControlDto controlDto = getControlFrom(requestTypeEnum, localDateTime, null, messageBodyDto.getRequestUuid());
        //to check
        controlDto.setEftiGateUrl(eftiGateUrl);
        controlDto.setFromGateUrl(fromGateUrl);
        controlDto.setTransportMetaData(SearchParameter.builder()
                .vehicleId(messageBodyDto.getVehicleID())
                .transportMode(messageBodyDto.getTransportMode())
                .vehicleCountry(messageBodyDto.getVehicleCountry())
                .isDangerousGoods(messageBodyDto.getIsDangerousGoods())
                .build());
        return controlDto;
    }

    private static ControlDto getControlFrom(String requestTypeEnum, LocalDateTime localDateTime, AuthorityDto authorityDto, String requestUuid) {
        ControlDto controlDto = new ControlDto();
        controlDto.setRequestUuid(requestUuid);
        controlDto.setRequestType(requestTypeEnum);
        controlDto.setStatus(PENDING.toString());
        controlDto.setSubsetEuRequested("SubsetEuRequested");
        controlDto.setSubsetMsRequested("SubsetMsRequested");
        controlDto.setCreatedDate(localDateTime);
        controlDto.setLastModifiedDate(localDateTime);
        controlDto.setAuthority(authorityDto);
        return controlDto;
    }

    public boolean isError() {
        return StatusEnum.ERROR.name().equals(status);
    }
}
